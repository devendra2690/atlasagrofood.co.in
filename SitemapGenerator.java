import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class SitemapGenerator {

    // Update this to your domain
    private static final String BASE_URL = "https://atlasagrofood.co.in";

    // What to include
    private static final Set<String> INCLUDE_EXT = Set.of(".html");

    // What to exclude (folders or file patterns)
    private static final List<String> EXCLUDE_DIR_CONTAINS = List.of(
            "/.git/",
            "/node_modules/",
            "/assets/",
            "/images/",
            "/img/",
            "/css/",
            "/js/",
            "/fonts/"
    );

    private static final List<String> EXCLUDE_FILE_NAMES = List.of(
            "404.html"
    );

    // If you have folders that are deployed but should not be indexed, add them here
    private static final List<String> EXCLUDE_PATH_CONTAINS = List.of(
            "/draft",
            "/temp",
            "/backup"
    );

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    public static void main(String[] args) throws Exception {
        // Usage:
        // java SitemapGenerator <repoRootPath> <outputFile>
        // Example:
        // java SitemapGenerator . sitemap.xml

        Path repoRoot = Paths.get(args.length > 0 ? args[0] : ".");
        Path outFile = Paths.get(args.length > 1 ? args[1] : "sitemap.xml");

        List<UrlEntry> urls = collectUrls(repoRoot);

        // Sort: homepage first, then shortest paths
        urls.sort(Comparator
                .comparing((UrlEntry e) -> e.loc.equals(BASE_URL + "/") ? 0 : 1)
                .thenComparing(e -> e.loc.length())
                .thenComparing(e -> e.loc)
        );

        writeSitemap(outFile, urls);

        System.out.println("✅ Generated sitemap with " + urls.size() + " URLs:");
        System.out.println("   " + outFile.toAbsolutePath());
    }

    private static List<UrlEntry> collectUrls(Path repoRoot) throws IOException {
        List<UrlEntry> list = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(repoRoot)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(p -> shouldInclude(repoRoot, p))
                    .forEach(p -> {
                        try {
                            UrlEntry entry = toUrlEntry(repoRoot, p);
                            if (entry != null) list.add(entry);
                        } catch (Exception ignored) {
                            // skip problematic files
                        }
                    });
        }

        // Ensure root homepage exists if index.html is present
        // (Handled by mapping index.html -> "/")
        return dedupeByLoc(list);
    }

    private static boolean shouldInclude(Path repoRoot, Path file) {
        String rel = normalizeSlashes(repoRoot.relativize(file).toString());

        // Exclude folders
        for (String badDir : EXCLUDE_DIR_CONTAINS) {
            if (rel.contains(badDir.replace("\\", "/"))) return false;
        }

        // Exclude specific path patterns
        for (String bad : EXCLUDE_PATH_CONTAINS) {
            if (rel.contains(bad)) return false;
        }

        // Exclude file names
        String name = file.getFileName().toString();
        for (String badName : EXCLUDE_FILE_NAMES) {
            if (name.equalsIgnoreCase(badName)) return false;
        }

        // Include only certain extensions
        String lower = name.toLowerCase(Locale.ROOT);
        for (String ext : INCLUDE_EXT) {
            if (lower.endsWith(ext)) return true;
        }
        return false;
    }

    private static UrlEntry toUrlEntry(Path repoRoot, Path file) throws IOException {
        String rel = normalizeSlashes(repoRoot.relativize(file).toString());

        // Convert Windows paths
        rel = rel.replace("\\", "/");

        String loc = toPublicUrl(rel);

        // lastmod from filesystem time (good enough for static sites)
        FileTime ft = Files.getLastModifiedTime(file);
        String lastmod = DATE_FMT.format(Instant.ofEpochMilli(ft.toMillis()).atZone(ZoneOffset.UTC).toLocalDate());

        // Changefreq/priority optional — keep simple and safe
        // You can tweak these rules later
        String changefreq = rel.contains("/products/") ? "weekly" : "monthly";
        String priority = rel.equalsIgnoreCase("index.html") ? "1.0" : (rel.contains("/products/") ? "0.8" : "0.6");

        return new UrlEntry(loc, lastmod, changefreq, priority);
    }

    private static String toPublicUrl(String rel) {
        // Map index.html to folder URL
        if (rel.equalsIgnoreCase("index.html")) {
            return BASE_URL + "/";
        }

        if (rel.toLowerCase(Locale.ROOT).endsWith("/index.html")) {
            String folder = rel.substring(0, rel.length() - "/index.html".length());
            return BASE_URL + "/" + folder + "/";
        }

        // Normal file -> direct URL
        return BASE_URL + "/" + rel;
    }

    private static void writeSitemap(Path outFile, List<UrlEntry> urls) throws IOException {
        Files.createDirectories(outFile.toAbsolutePath().getParent() == null ? Paths.get(".") : outFile.toAbsolutePath().getParent());

        try (BufferedWriter w = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
            w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            w.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

            for (UrlEntry e : urls) {
                w.write("  <url>\n");
                w.write("    <loc>" + escapeXml(e.loc) + "</loc>\n");
                w.write("    <lastmod>" + e.lastmod + "</lastmod>\n");
                w.write("    <changefreq>" + e.changefreq + "</changefreq>\n");
                w.write("    <priority>" + e.priority + "</priority>\n");
                w.write("  </url>\n");
            }

            w.write("</urlset>\n");
        }
    }

    private static List<UrlEntry> dedupeByLoc(List<UrlEntry> list) {
        Map<String, UrlEntry> map = new LinkedHashMap<>();
        for (UrlEntry e : list) {
            // Keep the newest lastmod if duplicates exist
            UrlEntry existing = map.get(e.loc);
            if (existing == null) {
                map.put(e.loc, e);
            } else {
                // Compare lastmod lexicographically (YYYY-MM-DD works)
                if (e.lastmod.compareTo(existing.lastmod) > 0) {
                    map.put(e.loc, e);
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private static String normalizeSlashes(String s) {
        return s.replace("\\", "/");
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static class UrlEntry {
        final String loc;
        final String lastmod;
        final String changefreq;
        final String priority;

        UrlEntry(String loc, String lastmod, String changefreq, String priority) {
            this.loc = loc;
            this.lastmod = lastmod;
            this.changefreq = changefreq;
            this.priority = priority;
        }
    }
}