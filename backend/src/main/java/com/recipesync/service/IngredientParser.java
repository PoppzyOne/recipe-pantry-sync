package com.recipesync.service;

import com.recipesync.dto.CreateRecipeIngredientDto;
import com.recipesync.entity.IngredientCategory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IngredientParser {

    private static final Map<String, String> CANONICAL_UNITS = new HashMap<>();
    private static final Pattern PARENTHESIS_PATTERN = Pattern.compile("\\(([^)]+)\\)");
    private static final Pattern LEADING_QUANTITY_PATTERN = Pattern.compile(
            "^\\s*(?:ca\\.?|cirka)?\\s*([0-9]+(?:[.,][0-9]+)?)\\s*([a-zA-ZåäöÅÄÖ]+)?\\s*(.*)$",
            Pattern.CASE_INSENSITIVE
    );

    static {
        // Gram / Weight
        registerUnit("g", "g", "gram", "grams");
        registerUnit("kg", "kg", "kilo", "kilogram", "kilograms");
        registerUnit("hg", "hg", "hektogram");
        registerUnit("mg", "mg", "milligram");

        // Volume
        registerUnit("l", "l", "liter", "litres", "liters");
        registerUnit("dl", "dl", "deciliter", "decilitre", "decilitres");
        registerUnit("cl", "cl", "centiliter", "centilitre");
        registerUnit("ml", "ml", "milliliter", "millilitre");

        // Spoon / Spices
        registerUnit("msk", "msk", "matsked", "matskedar", "tbsp", "tablespoon", "tablespoons");
        registerUnit("tsk", "tsk", "tesked", "teskedar", "tsp", "teaspoon", "teaspoons");
        registerUnit("krm", "krm", "kryddmått");

        // Pieces & Items
        registerUnit("st", "st", "styck", "stycken", "pc", "pcs", "piece", "pieces");
        registerUnit("klyfta", "klyfta", "klyftor", "clove", "cloves");
        registerUnit("burk", "burk", "burkar", "can", "cans", "tin", "tins");
        registerUnit("pkt", "pkt", "paket", "förpackning", "förpackningar", "fp", "fps", "påse", "påsar", "pack", "package");
        registerUnit("skiva", "skiva", "skivor", "slice", "slices");
        registerUnit("kruka", "kruka", "krukor", "pot", "pots");
        registerUnit("nypa", "nypa", "nypor", "pinch");
        registerUnit("droppe", "droppe", "droppar", "drop", "drops");
        registerUnit("port", "port", "portion", "portioner");
        registerUnit("näve", "näve", "nävar", "handful");
        registerUnit("kvist", "kvist", "kvistar", "sprig", "sprigs");
        registerUnit("stjälk", "stjälk", "stjälkar", "stalk", "stalks");
    }

    private static void registerUnit(String canonical, String... aliases) {
        for (String alias : aliases) {
            CANONICAL_UNITS.put(alias.toLowerCase(Locale.ROOT), canonical);
        }
    }

    private IngredientParser() {
    }

    public static CreateRecipeIngredientDto parse(String rawLine) {
        if (rawLine == null || rawLine.isBlank()) {
            return null;
        }

        String line = cleanString(rawLine);
        if (line.isBlank()) {
            return null;
        }

        // Extract any notes in parentheses e.g. "(à 400 g)" or "(finhackad)"
        String notes = null;
        Matcher parenMatcher = PARENTHESIS_PATTERN.matcher(line);
        if (parenMatcher.find()) {
            notes = parenMatcher.group(1).trim();
            line = parenMatcher.replaceFirst("").trim();
        }

        // Normalize fractions (e.g., "1 1/2", "1/2", "½", "1,5")
        line = normalizeFractions(line);

        Double amount = null;
        String unit = null;
        String name = line;

        Matcher matcher = LEADING_QUANTITY_PATTERN.matcher(line);
        if (matcher.matches()) {
            String numStr = matcher.group(1);
            String wordCandidate = matcher.group(2);
            String rest = matcher.group(3);

            if (numStr != null) {
                try {
                    amount = Double.parseDouble(numStr.replace(',', '.'));
                } catch (NumberFormatException ignored) {
                }
            }

            if (wordCandidate != null) {
                String wordLower = wordCandidate.toLowerCase(Locale.ROOT);
                if (CANONICAL_UNITS.containsKey(wordLower)) {
                    unit = CANONICAL_UNITS.get(wordLower);
                    name = rest != null ? rest.trim() : "";
                } else {
                    // Not a recognized unit word, so the word is part of the ingredient name
                    name = (wordCandidate + " " + (rest != null ? rest : "")).trim();
                    // If an amount was provided but no unit, default to 'st' if amount > 0
                    if (amount != null) {
                        unit = "st";
                    }
                }
            } else if (rest != null && !rest.isBlank()) {
                name = rest.trim();
                if (amount != null) {
                    unit = "st";
                }
            }
        }

        // Clean up name
        name = cleanName(name);
        if (name.isBlank()) {
            name = cleanName(rawLine);
        }

        IngredientCategory category = detectCategory(name);

        return new CreateRecipeIngredientDto(
                capitalizeFirst(name),
                category,
                amount,
                unit,
                notes != null && !notes.isBlank() ? notes : null
        );
    }

    private static String cleanString(String s) {
        return s.replace('\u00A0', ' ')
                .replace('\u200B', ' ')
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private static String normalizeFractions(String text) {
        String s = text
                .replace("½", "0.5")
                .replace("¼", "0.25")
                .replace("¾", "0.75")
                .replace("⅓", "0.33")
                .replace("⅔", "0.67")
                .replace("⅛", "0.125");

        // Match "1 1/2" or "2 1/4" -> 2.25
        Pattern mixedFraction = Pattern.compile("(\\d+)\\s+(\\d+)/(\\d+)");
        Matcher m = mixedFraction.matcher(s);
        if (m.find()) {
            double whole = Double.parseDouble(m.group(1));
            double num = Double.parseDouble(m.group(2));
            double den = Double.parseDouble(m.group(3));
            double total = whole + (num / den);
            s = m.replaceFirst(String.format(Locale.ROOT, "%.2f", total).replaceAll("\\.?0+$", ""));
        }

        // Match "1/2" -> 0.5
        Pattern singleFraction = Pattern.compile("(\\d+)/(\\d+)");
        Matcher m2 = singleFraction.matcher(s);
        if (m2.find()) {
            double num = Double.parseDouble(m2.group(1));
            double den = Double.parseDouble(m2.group(2));
            double total = num / den;
            s = m2.replaceFirst(String.format(Locale.ROOT, "%.2f", total).replaceAll("\\.?0+$", ""));
        }

        // Match range like "1-2" -> 1
        Pattern rangePattern = Pattern.compile("^(\\d+)\\s*-\\s*\\d+");
        Matcher m3 = rangePattern.matcher(s);
        if (m3.find()) {
            s = m3.replaceFirst(m3.group(1));
        }

        return s;
    }

    private static String cleanName(String name) {
        String cleaned = name
                .replaceAll("^[-–•*]\\s*", "")
                .replaceAll("^[àa]\\s+", "")
                .replaceAll(",\\s*$", "")
                .trim();
        return cleaned;
    }

    private static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static IngredientCategory detectCategory(String ingredientName) {
        if (ingredientName == null) return IngredientCategory.OTHER;
        String lower = ingredientName.toLowerCase(Locale.ROOT);

        // Meat, poultry, fish & seafood
        if (matchesAny(lower, "kyckling", "nötfärs", "blandfärs", "fläskfärs", "köttfärs", "oxfilé", "fläskfilé",
                "entrecote", "lövbiff", "biff", "bacon", "korv", "falukorv", "prinskorv", "skinka", "kassler",
                "lax", "torsk", "räkor", "tonfisk", "sill", "sej", "fisk", "kött", "lamm", "kalkon",
                "meat", "chicken", "beef", "pork", "sausage", "salmon", "shrimp", "tuna")) {
            return IngredientCategory.MEAT;
        }

        // Dairy & cheese
        if (matchesAny(lower, "mjölk", "fil", "grädde", "vispgrädde", "matlagningsgrädde", "smör", "ost",
                "parmesan", "crème fraiche", "creme fraiche", "kvarg", "yoghurt", "fetaost", "feta",
                "mozzarella", "halloumi", "ricotta", "cheddar", "gouda", "prästost", "västerbotten",
                "ägg", "egg", "dairy", "milk", "cream", "butter", "cheese")) {
            return IngredientCategory.DAIRY;
        }

        // Spices & Seasonings
        if (matchesAny(lower, "salt", "havssalt", "flingsalt", "peppar", "svartpeppar", "vitpeppar",
                "cayennepeppar", "paprikapulver", "spiskummin", "oregano", "curry", "garam masala",
                "kardemumma", "kanel", "kryddnejlika", "muskot", "ingefärspulver", "vaniljsocker",
                "saffran", "lagerblad", "senap", "dijonsenap", "ketchup", "tabasco", "sambal oelek",
                "spice", "spices")) {
            return IngredientCategory.SPICES;
        }

        // Pantry staples (canned goods, grains, flours, oils)
        if (matchesAny(lower, "pasta", "spagetti", "spaghetti", "makaroner", "penne", "fusilli", "tagliatelle",
                "fettuccine", "linguine", "lasagne", "gnocchi", "rigatoni", "farfalle", "ris",
                "basmatiris", "jasminris", "havregryn", "couscous", "bulgur", "quinoa", "nudlar", "mjöl",
                "vetemjöl", "rågmjöl", "majsstärkelse", "maizena", "ströbröd", "olivolja", "rapsolja",
                "olja", "sesamolja", "krossade tomater", "passerade tomater", "tomatpuré", "kokosmjölk",
                "bönor", "svarta bönor", "kidneybönor", "linser", "kikärtor", "soja", "sojasås", "vinäger",
                "vitvinsvinäger", "balsamvinäger", "äppelcidervinäger", "honung", "jordnötssmör", "mandel",
                "valnötter", "cashewnötter", "buljong", "buljongtärning", "fond", "pantry", "flour", "rice",
                "oil", "vinegar", "beans", "lentils")) {
            return IngredientCategory.PANTRY;
        }

        // Produce (Fresh fruits & vegetables)
        if (matchesAny(lower, "lök", "vitlök", "purjolök", "schalottenlök", "rödlök", "salladslök", "potatis",
                "sötpotatis", "morot", "morötter", "tomat", "tomater", "körsbärstomater", "spenat", "ruccola",
                "sallad", "gurka", "paprika", "chili", "chilipeppar", "jalapeño", "zucchini", "squash",
                "aubergine", "broccoli", "blomkål", "sparris", "svamp", "champinjoner", "kantareller",
                "citron", "citroner", "lime", "apelsin", "äpple", "äpplen", "banan", "bananer", "päron",
                "jordgubbar", "hallon", "blåbär", "ingefära", "basilika", "persilja", "koriander", "dill",
                "gräslök", "timjan", "rosmarin", "mynta", "avokado", "ärtskott", "vegetable", "fruit",
                "onion", "garlic", "tomato", "potato", "carrot", "spinach", "cucumber", "lemon", "mushroom")) {
            return IngredientCategory.PRODUCE;
        }

        // Bakery
        if (matchesAny(lower, "bröd", "surdegsbröd", "rostbröd", "baguette", "tortillabröd", "tortillas",
                "pitabröd", "hamburgerbröd", "korvbröd", "jäst", "bakpulver", "bikarbonat", "socker",
                "strösocker", "florsocker", "sirap", "kakao", "bread", "yeast", "sugar")) {
            return IngredientCategory.BAKERY;
        }

        // Frozen
        if (matchesAny(lower, "fryst", "frysta", "ärter", "gröna ärter", "fryst mango", "glass", "frozen")) {
            return IngredientCategory.FROZEN;
        }

        return IngredientCategory.OTHER;
    }

    private static boolean matchesAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
