package com.fpvdrone.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The "poly_mesh" geometry of Bedrock model files, per bone. The fixed-wing airframes (bedrock_geo/*.geo.json: FP-1,
 * Lancet, Orlan) model their fuselage and some control surfaces as poly meshes.
 * <p>
 * 1.1.8 drew them with the simplebedrockmodel library (BedrockPolyMesh, triangles drawn with its polyMeshCutout render
 * type). GeckoLib 5.5.7 parses poly meshes but does not bake them into its bones, so {@link BedrockModels} alone gives
 * only the cubes. This class reads the same file again and bakes each bone's polygons into triangles the way the
 * library did: X mirrored, quads split 0-1-2 / 2-3-0, other polygons fanned, a closing vertex equal to the first dropped,
 * degenerate triangles skipped, missing normals replaced by the face normal, V flipped. Positions are absolute model
 * coordinates in blocks (GeckoLib's frame, where a bone's matrix already backs off its pivot), unlike the library's
 * pivot-relative ones. Cleared with {@link BedrockModels} on every resource reload.
 */
public final class BedrockPolyMeshes {
    private static final Logger LOGGER = LoggerFactory.getLogger("FPVDrone/PolyMesh");
    /** Floats per vertex: x, y, z, u, v, nx, ny, nz. */
    public static final int STRIDE = 8;
    private static final Map<Identifier, Map<String, float[]>> CACHE = new ConcurrentHashMap<>();

    private BedrockPolyMeshes() {
    }

    /** Triangles per bone name (3 vertices of {@link #STRIDE} floats each); empty when the file has no poly meshes. */
    public static Map<String, float[]> get(Identifier model) {
        return CACHE.computeIfAbsent(model, BedrockPolyMeshes::load);
    }

    public static void clear() {
        CACHE.clear();
    }

    private static Map<String, float[]> load(Identifier id) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id);
        if (resource.isEmpty()) {
            return Map.of();
        }

        try (Reader reader = resource.get().openAsReader()) {
            JsonReader json = new JsonReader(reader);
            json.setStrictness(com.google.gson.Strictness.LENIENT);
            JsonObject root = JsonParser.parseReader(json).getAsJsonObject();
            JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
            if (geometries == null || geometries.isEmpty()) {
                return Map.of();
            }

            JsonObject geometry = geometries.get(0).getAsJsonObject();
            JsonObject description = geometry.has("description") ? geometry.getAsJsonObject("description") : new JsonObject();
            float texWidth = description.has("texture_width") ? description.get("texture_width").getAsFloat() : 0.0F;
            float texHeight = description.has("texture_height") ? description.get("texture_height").getAsFloat() : 0.0F;
            JsonArray bones = geometry.getAsJsonArray("bones");
            if (bones == null) {
                return Map.of();
            }

            Map<String, float[]> result = new HashMap<>();
            for (JsonElement element : bones) {
                JsonObject bone = element.getAsJsonObject();
                if (bone.has("poly_mesh") && bone.has("name")) {
                    float[] triangles = bake(bone.getAsJsonObject("poly_mesh"), texWidth, texHeight);
                    if (triangles.length > 0) {
                        result.put(bone.get("name").getAsString(), triangles);
                    }
                }
            }

            return result.isEmpty() ? Map.of() : Map.copyOf(result);
        } catch (Exception e) {
            LOGGER.warn("Could not read the poly meshes of {}: {}", id, e.toString());
            return Map.of();
        }
    }

    private static float[] bake(JsonObject mesh, float texWidth, float texHeight) {
        float[][] positions = floats(mesh.getAsJsonArray("positions"));
        float[][] normals = floats(mesh.getAsJsonArray("normals"));
        float[][] uvs = floats(mesh.getAsJsonArray("uvs"));
        boolean normalizedUvs = mesh.has("normalized_uvs") && mesh.get("normalized_uvs").getAsBoolean();
        JsonElement polys = mesh.get("polys");
        List<float[]> triangles = new ArrayList<>();
        if (positions == null || polys == null || polys.isJsonNull()) {
            return new float[0];
        }

        if (polys.isJsonPrimitive()) {
            String mode = polys.getAsString();
            int stride = "tri_list".equals(mode) ? 3 : "quad_list".equals(mode) ? 4 : 0;
            for (int i = 0; stride > 0 && i + stride - 1 < positions.length; i += stride) {
                List<float[]> polygon = new ArrayList<>(stride);
                for (int j = 0; j < stride; j++) {
                    polygon.add(vertex(positions, normals, uvs, i + j, i + j, i + j, normalizedUvs, texWidth, texHeight));
                }

                polygon(polygon, triangles);
            }
        } else if (polys.isJsonArray()) {
            for (JsonElement polyElement : polys.getAsJsonArray()) {
                if (!polyElement.isJsonArray()) {
                    continue;
                }

                List<float[]> polygon = new ArrayList<>();
                for (JsonElement v : polyElement.getAsJsonArray()) {
                    if (v.isJsonArray() && !v.getAsJsonArray().isEmpty()) {
                        JsonArray idx = v.getAsJsonArray();
                        int p = idx.get(0).getAsInt();
                        int n = idx.size() > 1 ? idx.get(1).getAsInt() : p;
                        int t = idx.size() > 2 ? idx.get(2).getAsInt() : p;
                        polygon.add(vertex(positions, normals, uvs, p, n, t, normalizedUvs, texWidth, texHeight));
                    }
                }

                if (polygon.size() > 1 && samePosition(polygon.get(0), polygon.get(polygon.size() - 1))) {
                    polygon.remove(polygon.size() - 1);
                }

                polygon(polygon, triangles);
            }
        }

        float[] out = new float[triangles.size() * STRIDE];
        for (int i = 0; i < triangles.size(); i++) {
            System.arraycopy(triangles.get(i), 0, out, i * STRIDE, STRIDE);
        }

        return out;
    }

    private static void polygon(List<float[]> polygon, List<float[]> out) {
        if (polygon.size() < 3) {
            return;
        }

        if (polygon.size() == 4) {
            triangle(polygon.get(0), polygon.get(1), polygon.get(2), out);
            triangle(polygon.get(2), polygon.get(3), polygon.get(0), out);
        } else {
            for (int i = 1; i + 1 < polygon.size(); i++) {
                triangle(polygon.get(0), polygon.get(i), polygon.get(i + 1), out);
            }
        }
    }

    private static void triangle(float[] a, float[] b, float[] c, List<float[]> out) {
        if (samePosition(a, b) || samePosition(b, c) || samePosition(c, a)) {
            return;
        }

        Vector3f normal = new Vector3f(b[0] - a[0], b[1] - a[1], b[2] - a[2]).cross(c[0] - a[0], c[1] - a[1], c[2] - a[2]);
        if (normal.lengthSquared() < 1.0E-12F) {
            normal.set(0.0F, 1.0F, 0.0F);
        } else {
            normal.normalize();
        }

        for (float[] v : new float[][]{a, b, c}) {
            float[] copy = v.clone();
            if (copy[5] * copy[5] + copy[6] * copy[6] + copy[7] * copy[7] <= 1.0E-12F) {
                copy[5] = normal.x;
                copy[6] = normal.y;
                copy[7] = normal.z;
            }

            out.add(copy);
        }
    }

    private static float[] vertex(
        float[][] positions, float[][] normals, float[][] uvs, int p, int n, int t, boolean normalizedUvs, float texWidth, float texHeight
    ) {
        float[] pos = p >= 0 && p < positions.length && positions[p] != null ? positions[p] : new float[3];
        float[] v = new float[STRIDE];
        v[0] = -get(pos, 0) / 16.0F;
        v[1] = get(pos, 1) / 16.0F;
        v[2] = get(pos, 2) / 16.0F;
        if (uvs != null && t >= 0 && t < uvs.length) {
            float u = get(uvs[t], 0);
            float w = get(uvs[t], 1);
            if (!normalizedUvs) {
                u = texWidth == 0.0F ? 0.0F : u / texWidth;
                w = texHeight == 0.0F ? 0.0F : w / texHeight;
            }

            v[3] = u;
            v[4] = 1.0F - w;
        }

        if (normals != null && n >= 0 && n < normals.length) {
            float nx = -get(normals[n], 0);
            float ny = get(normals[n], 1);
            float nz = get(normals[n], 2);
            float length = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (length > 1.0E-6F) {
                v[5] = nx / length;
                v[6] = ny / length;
                v[7] = nz / length;
            }
        }

        return v;
    }

    private static boolean samePosition(float[] a, float[] b) {
        // The library compared pivot-relative pixel/16 values; absolute ones differ only by a constant.
        return Math.abs(a[0] - b[0]) < 1.0E-6F && Math.abs(a[1] - b[1]) < 1.0E-6F && Math.abs(a[2] - b[2]) < 1.0E-6F;
    }

    private static float get(float[] values, int i) {
        return values != null && i < values.length ? values[i] : 0.0F;
    }

    private static float[][] floats(JsonArray array) {
        if (array == null) {
            return null;
        }

        float[][] out = new float[array.size()][];
        for (int i = 0; i < array.size(); i++) {
            JsonElement e = array.get(i);
            if (e.isJsonArray()) {
                JsonArray a = e.getAsJsonArray();
                out[i] = new float[a.size()];
                for (int j = 0; j < a.size(); j++) {
                    out[i][j] = a.get(j).getAsFloat();
                }
            }
        }

        return out;
    }
}
