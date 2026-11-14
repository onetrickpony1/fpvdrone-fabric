package com.fpvdrone.client.particle;

import com.fpvdrone.data.ParticleSpec;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 1.1.8-PENDING: ProjectileDefinitions (synced from the server) owns each projectile's "particles" list. Until it is
// ported, the bundled data/fpvdrone/projectiles/<id>.json is read straight from the mod jar.
final class ProjectileParticles {
    private static final Logger LOGGER = LoggerFactory.getLogger("FPVDrone/Particles");
    private static final Map<String, List<ParticleSpec>> CACHE = new ConcurrentHashMap<>();

    private ProjectileParticles() {
    }

    static List<ParticleSpec> forProjectile(String id) {
        if (id == null || id.isEmpty() || !id.matches("[a-z0-9_.-]+")) {
            return List.of();
        }

        return CACHE.computeIfAbsent(id, ProjectileParticles::load);
    }

    private static List<ParticleSpec> load(String id) {
        String path = "/data/fpvdrone/projectiles/" + id + ".json";

        try (InputStream is = ProjectileParticles.class.getResourceAsStream(path)) {
            if (is == null) {
                return List.of();
            }

            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(stripLineComments(text)).getAsJsonObject();
            return ParticleSpec.listFrom(json, "particles");
        } catch (Exception e) {
            LOGGER.warn("Could not read particles of projectile {}: {}", id, e.getMessage());
            return List.of();
        }
    }

    private static String stripLineComments(String text) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                sb.append(c);
                escaped = false;
            } else if (c == '\\' && inString) {
                sb.append(c);
                escaped = true;
            } else if (c == '"') {
                inString = !inString;
                sb.append(c);
            } else if (!inString && c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                while (i < text.length() && text.charAt(i) != '\n') {
                    i++;
                }

                sb.append('\n');
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
