#!/usr/bin/env bash
# ==============================================================================
# Recipe & Pantry Sync - SSH Deployment Script
# 
# Usage:
#   ./scripts/deploy.sh [user@ip]
#   ./scripts/deploy.sh --host user@ip --branch main
#   DEPLOY_HOST="user@ip" ./scripts/deploy.sh
# ==============================================================================

set -euo pipefail

# ANSI Color Codes
COLOR_RESET="\033[0m"
COLOR_BOLD="\033[1m"
COLOR_GREEN="\033[32m"
COLOR_BLUE="\033[34m"
COLOR_YELLOW="\033[33m"
COLOR_RED="\033[31m"
COLOR_CYAN="\033[36m"

log_info() {
    echo -e "${COLOR_BLUE}[INFO]${COLOR_RESET} $*"
}

log_step() {
    echo -e "\n${COLOR_BOLD}${COLOR_CYAN}==>${COLOR_RESET} ${COLOR_BOLD}$*${COLOR_RESET}"
}

log_success() {
    echo -e "${COLOR_GREEN}[SUCCESS]${COLOR_RESET} $*"
}

log_warn() {
    echo -e "${COLOR_YELLOW}[WARNING]${COLOR_RESET} $*"
}

log_error() {
    echo -e "${COLOR_RED}[ERROR]${COLOR_RESET} $*" >&2
}

# Determine script and project directories
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# Load configuration from environment files if available
if [[ -f "${SCRIPT_DIR}/.deploy.env" ]]; then
    # shellcheck disable=SC1091
    source "${SCRIPT_DIR}/.deploy.env"
elif [[ -f "${PROJECT_ROOT}/.deploy.env" ]]; then
    # shellcheck disable=SC1091
    source "${PROJECT_ROOT}/.deploy.env"
fi

# Default configuration
DEPLOY_HOST="${DEPLOY_HOST:-}"
DEPLOY_DIR="${DEPLOY_DIR:-/home/poppe/repos/recipe-pantry-sync}"
DEPLOY_BRANCH="${DEPLOY_BRANCH:-main}"
SSH_PORT="${SSH_PORT:-22}"
SKIP_PULL="${SKIP_PULL:-false}"
SKIP_PRUNE="${SKIP_PRUNE:-false}"

show_help() {
    cat << EOF
Recipe & Pantry Sync - Deployment Script

Usage:
  $(basename "$0") [options] [user@host]

Arguments:
  [user@host]                  SSH target (e.g. poppe@192.168.1.50 or SSH config alias)

Options:
  -h, --help                   Show this help message and exit
  --host <user@host>           Specify SSH destination host
  --dir <path>                 Remote project path (default: /home/poppe/repos/recipe-pantry-sync)
  --branch <branch>            Git branch to pull (default: main)
  -p, --port <port>            SSH port (default: 22)
  --skip-pull                  Skip 'git pull' step on remote
  --skip-prune                 Skip 'docker image prune -f' step

Configuration via File:
  You can create 'scripts/.deploy.env' (based on 'scripts/.deploy.env.example')
  to persist your settings without typing them every time.

Examples:
  ./scripts/deploy.sh poppe@192.168.1.50
  ./scripts/deploy.sh --branch develop poppe@myserver
  ./scripts/deploy.sh --skip-pull
EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        -h|--help)
            show_help
            exit 0
            ;;
        --host)
            DEPLOY_HOST="$2"
            shift 2
            ;;
        --dir)
            DEPLOY_DIR="$2"
            shift 2
            ;;
        --branch)
            DEPLOY_BRANCH="$2"
            shift 2
            ;;
        -p|--port)
            SSH_PORT="$2"
            shift 2
            ;;
        --skip-pull)
            SKIP_PULL=true
            shift
            ;;
        --skip-prune)
            SKIP_PRUNE=true
            shift
            ;;
        -*)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
        *)
            # Positional argument for host
            DEPLOY_HOST="$1"
            shift
            ;;
    esac
done

# Step 0: Validate prerequisites
if [[ -z "${DEPLOY_HOST}" ]]; then
    log_error "No deployment host specified!"
    echo ""
    echo "Please provide your server destination using one of the following methods:"
    echo "  1. CLI argument:     ./scripts/deploy.sh poppe@<your-server-ip>"
    echo "  2. Environment var:  export DEPLOY_HOST=\"poppe@<your-server-ip>\""
    echo "  3. Config file:      Copy 'scripts/.deploy.env.example' to 'scripts/.deploy.env' and fill in DEPLOY_HOST"
    echo ""
    exit 1
fi

if ! command -v ssh >/dev/null 2>&1; then
    log_error "'ssh' command is not installed or not in PATH."
    exit 1
fi

# Print deployment summary
echo -e "${COLOR_BOLD}======================================================${COLOR_RESET}"
echo -e "${COLOR_BOLD} Recipe & Pantry Sync - Deployment${COLOR_RESET}"
echo -e "${COLOR_BOLD}======================================================${COLOR_RESET}"
log_info "Target Host:       ${COLOR_CYAN}${DEPLOY_HOST}${COLOR_RESET}"
log_info "SSH Port:          ${SSH_PORT}"
log_info "Remote Directory:  ${DEPLOY_DIR}"
log_info "Target Branch:     ${DEPLOY_BRANCH}"
log_info "Skip Git Pull:     ${SKIP_PULL}"
log_info "Skip Image Prune:  ${SKIP_PRUNE}"
echo -e "${COLOR_BOLD}======================================================${COLOR_RESET}"

# Base SSH command with port
SSH_CMD=(ssh -p "${SSH_PORT}" "${DEPLOY_HOST}")

# Step 1: Pre-flight check on remote host
log_step "Step 1/5: Checking SSH connection and remote directory..."
if ! "${SSH_CMD[@]}" "test -d '${DEPLOY_DIR}'" 2>/dev/null; then
    log_error "Could not access remote directory '${DEPLOY_DIR}' on ${DEPLOY_HOST}."
    log_error "Please ensure SSH access works and the repository has been cloned to that path."
    exit 1
fi
log_success "SSH connection established and remote directory verified."

# Step 2: Git pull on remote server
if [[ "${SKIP_PULL}" == "false" ]]; then
    log_step "Step 2/5: Fetching latest changes from branch '${DEPLOY_BRANCH}'..."
    "${SSH_CMD[@]}" bash << REMOTE_BASH
        set -euo pipefail
        cd "${DEPLOY_DIR}"
        echo "Updating repository from origin/${DEPLOY_BRANCH}..."
        git fetch origin "${DEPLOY_BRANCH}"
        git checkout "${DEPLOY_BRANCH}"
        git pull --ff-only origin "${DEPLOY_BRANCH}"
        echo "Current commit: \$(git log -1 --oneline)"
REMOTE_BASH
    log_success "Git pull completed successfully."
else
    log_step "Step 2/5: Skipping git pull as requested (--skip-pull)."
fi

# Step 3: Build & restart containers with Docker Compose
log_step "Step 3/5: Rebuilding and restarting containers via docker compose..."
"${SSH_CMD[@]}" bash << REMOTE_BASH
    set -euo pipefail
    cd "${DEPLOY_DIR}"
    echo "Executing: docker compose up -d --build --remove-orphans"
    docker compose up -d --build --remove-orphans
REMOTE_BASH
log_success "Docker Compose command completed."

# Step 4: Verify running containers
log_step "Step 4/5: Checking container status..."
"${SSH_CMD[@]}" bash << REMOTE_BASH
    set -euo pipefail
    cd "${DEPLOY_DIR}"
    docker compose ps
REMOTE_BASH

# Step 5: Clean up dangling images
if [[ "${SKIP_PRUNE}" == "false" ]]; then
    log_step "Step 5/5: Pruning dangling Docker images to preserve disk space..."
    "${SSH_CMD[@]}" "docker image prune -f" || log_warn "Image prune reported non-zero status, continuing."
    log_success "Disk cleanup completed."
else
    log_step "Step 5/5: Skipping image prune (--skip-prune)."
fi

echo -e "\n${COLOR_BOLD}${COLOR_GREEN}======================================================${COLOR_RESET}"
echo -e "${COLOR_BOLD}${COLOR_GREEN} Deployment Completed Successfully!${COLOR_RESET}"
echo -e "${COLOR_BOLD}${COLOR_GREEN}======================================================${COLOR_RESET}\n"
