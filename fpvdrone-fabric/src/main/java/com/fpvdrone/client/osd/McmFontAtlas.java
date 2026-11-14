package com.fpvdrone.client.osd;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.NativeImage.Format;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public final class McmFontAtlas {
    public static final int CHAR_WIDTH = 12;
    public static final int CHAR_HEIGHT = 18;
    public static final int ATLAS_COLS = 16;
    public static final int MAX_CHARS = 512;
    private static McmFontAtlas instance;
    private Identifier atlasLocation;
    private DynamicTexture atlasTexture;
    private int charCount;
    private boolean loaded;
    private Identifier outlinedAtlasLocation;
    private DynamicTexture outlinedAtlasTexture;
    private static final int LOGO_START = 160;
    private static final int LOGO_TILES = 120;
    public static final int LOGO_TILE_COLS = 24;
    public static final int LOGO_TILE_ROWS = 5;

    private McmFontAtlas() {
    }

    public static McmFontAtlas getInstance() {
        if (instance == null) {
            instance = new McmFontAtlas();
        }

        return instance;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public Identifier getAtlasLocation() {
        return this.atlasLocation;
    }

    public Identifier getOutlinedAtlasLocation() {
        return this.outlinedAtlasLocation;
    }

    public int getCharCount() {
        return this.charCount;
    }

    public void loadFromResource(String resourcePath) {
        if (!this.loaded) {
            try {
                InputStream is = McmFontAtlas.class.getResourceAsStream(resourcePath);
                if (is == null) {
                    Identifier mcmRes = Identifier.fromNamespaceAndPath("fpvdrone", "font/monocraft.mcm");
                    is = ((Resource)Minecraft.getInstance().getResourceManager().getResource(mcmRes).orElseThrow()).open();
                }

                List<int[]> characters = this.parseMcm(is);
                is.close();
                patchLogoTiles(characters);
                this.charCount = characters.size();
                NativeImage plainImage = this.buildAtlasImage(characters, false);
                this.atlasTexture = new DynamicTexture(() -> "fpvdrone_osd_atlas", plainImage);
                this.atlasLocation = Identifier.fromNamespaceAndPath("fpvdrone", "dynamic/osd_atlas");
                Minecraft.getInstance().getTextureManager().register(this.atlasLocation, this.atlasTexture);
                NativeImage outlinedImage = this.buildAtlasImage(characters, true);
                this.outlinedAtlasTexture = new DynamicTexture(() -> "fpvdrone_osd_atlas_outlined", outlinedImage);
                this.outlinedAtlasLocation = Identifier.fromNamespaceAndPath("fpvdrone", "dynamic/osd_atlas_outlined");
                Minecraft.getInstance().getTextureManager().register(this.outlinedAtlasLocation, this.outlinedAtlasTexture);
                this.loaded = true;
            } catch (Exception var6) {
            }
        }
    }

    private List<int[]> parseMcm(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String header = reader.readLine();
        if (header != null && header.trim().equals("MAX7456")) {
            List<int[]> characters = new ArrayList<>();
            List<Integer> charBits = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 8) {
                    for (int i = 0; i < 8; i += 2) {
                        int v = Integer.parseInt(line.substring(i, i + 2), 2);
                        charBits.add(v);
                    }

                    if (charBits.size() >= 256) {
                        int[] pixels = new int[216];

                        for (int i = 0; i < pixels.length && i < charBits.size(); i++) {
                            pixels[i] = charBits.get(i);
                        }

                        characters.add(pixels);
                        charBits.clear();
                    }
                }
            }

            if (charBits.size() >= 216) {
                int[] pixels = new int[216];

                for (int i = 0; i < pixels.length; i++) {
                    pixels[i] = charBits.get(i);
                }

                characters.add(pixels);
            }

            reader.close();
            return characters;
        } else {
            throw new IllegalArgumentException("Invalid MCM: missing MAX7456 header");
        }
    }

    private NativeImage buildAtlasImage(List<int[]> characters, boolean outlined) {
        int rows = (characters.size() + 16 - 1) / 16;
        int cellW = outlined ? 14 : 12;
        int cellH = outlined ? 20 : 18;
        int imgW = 16 * cellW;
        int imgH = rows * cellH;
        NativeImage image = new NativeImage(Format.RGBA, imgW, imgH, false);

        for (int y = 0; y < imgH; y++) {
            for (int x = 0; x < imgW; x++) {
                image.setPixelABGR(x, y, 0);
            }
        }

        int WHITE = -1;
        int BLACK = -16777216;

        for (int ci = 0; ci < characters.size(); ci++) {
            int[] pixels = characters.get(ci);
            int col = ci % 16;
            int row = ci / 16;
            int ox = col * cellW + (outlined ? 1 : 0);
            int oy = row * cellH + (outlined ? 1 : 0);
            if (outlined) {
                for (int py = 0; py < 18; py++) {
                    for (int px = 0; px < 12; px++) {
                        int v = pixels[py * 12 + px];
                        if (v == 2) {
                            int[][] dirs = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

                            for (int[] d : dirs) {
                                int nx = px + d[0];
                                int ny = py + d[1];
                                int imgX = ox + nx;
                                int imgY = oy + ny;
                                if (imgX >= col * cellW
                                    && imgX < (col + 1) * cellW
                                    && imgY >= row * cellH
                                    && imgY < (row + 1) * cellH
                                    && (nx < 0 || nx >= 12 || ny < 0 || ny >= 18 || pixels[ny * 12 + nx] == 1 || pixels[ny * 12 + nx] == 3)) {
                                    image.setPixelABGR(imgX, imgY, BLACK);
                                }
                            }
                        }
                    }
                }
            }

            for (int py = 0; py < 18; py++) {
                for (int pxx = 0; pxx < 12; pxx++) {
                    int v = pixels[py * 12 + pxx];
                    if (v == 0) {
                        image.setPixelABGR(ox + pxx, oy + py, BLACK);
                    } else if (v == 2) {
                        image.setPixelABGR(ox + pxx, oy + py, WHITE);
                    }
                }
            }
        }

        return image;
    }

    public float[] getPlainUV(int charCode) {
        if (charCode < 0 || charCode >= this.charCount) {
            charCode = 32;
        }

        int col = charCode % 16;
        int row = charCode / 16;
        int atlasRows = (this.charCount + 16 - 1) / 16;
        float imgW = 192.0F;
        float imgH = atlasRows * 18;
        float u0 = col * 12 / imgW;
        float v0 = row * 18 / imgH;
        float u1 = (col + 1) * 12 / imgW;
        float v1 = (row + 1) * 18 / imgH;
        return new float[]{u0, v0, u1, v1};
    }

    public float[] getOutlinedUV(int charCode) {
        if (charCode < 0 || charCode >= this.charCount) {
            charCode = 32;
        }

        int cellW = 14;
        int cellH = 20;
        int col = charCode % 16;
        int row = charCode / 16;
        int atlasRows = (this.charCount + 16 - 1) / 16;
        float imgW = 16 * cellW;
        float imgH = atlasRows * cellH;
        float u0 = col * cellW / imgW;
        float v0 = row * cellH / imgH;
        float u1 = (col + 1) * cellW / imgW;
        float v1 = (row + 1) * cellH / imgH;
        return new float[]{u0, v0, u1, v1};
    }

    public int getPlainAtlasWidth() {
        return 192;
    }

    public int getPlainAtlasHeight() {
        int rows = (this.charCount + 16 - 1) / 16;
        return rows * 18;
    }

    public int getOutlinedAtlasWidth() {
        return 224;
    }

    public int getOutlinedAtlasHeight() {
        int rows = (this.charCount + 16 - 1) / 16;
        return rows * 20;
    }

    public void close() {
        if (this.atlasTexture != null) {
            this.atlasTexture.close();
            this.atlasTexture = null;
        }

        if (this.outlinedAtlasTexture != null) {
            this.outlinedAtlasTexture.close();
            this.outlinedAtlasTexture = null;
        }

        this.loaded = false;
        instance = null;
    }

    private static void patchLogoTiles(List<int[]> characters) {
        while (characters.size() <= 280) {
            characters.add(new int[216]);
        }

        String[] tileData = new String[]{
            "5555555555555555555555555555555555555555555555555555555555555555555555555555555550005550005550aa5550aa555080",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555000000000000aaaaaaaaaaaaaa8000",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555000000000000aaaaaaaaaaaa000000",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555000000000000aaaaaaaaaaaa02a800",
            "5555555555555555005554aa5542aa550aaa5400005554aa5554aa554000552aa2552aa2552aa200aa8a02aa28a2aa28a2aa280aa8a2",
            "5555554000002a2aaaaa2aaaa8aaaaa8aaaa000000aaaaaaaaaaaa0000008a2a8a2a288a2a2a8a28aa88a8aa8aa8aa8aa8aa8aa2aa8a",
            "555555000000aaaaa2aaaaa2aaaa8aaaaa8a000000aaaaaaaaaaaa000000aaaaaa8aaaaa2aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "555555000000aaaaa8aaaaa8aaaaa2aaaaa2000000aa2aaaaa2aaa2a2a008a28aa8a28a88a28a28a288a8a28aa8a28aa8a28aa8a28aa",
            "555555000000aaaaaaaaaaaaaaaaa8aaaaa8000000aaaaaaaaaaaa000002aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8",
            "5555550000002aaaaa2aaaaaaaaaaaaaaaaa000000a2aaaaa2aaaaa28000a22aaaa22a2aa228aaa222aaa22aaaa22aaaa22aaaa22aaa",
            "555555000000aaaa8aaaaa8aaaaa2aaaaa2a000000aaaaaaaaaaaa000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "555555000000aaaaaaaaaaaaaaaaaaaaaaaa000000aaaaaaaaaaaa000000a2aaaaa28aaaa22aaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaa",
            "555555000000aa2aaaaa2aaaa8aaaaa8aaaa000000a2aaaaa2aaaaa2a000228aaa228a2a2288aa228aaa228aaa228aaa228aaa228aaa",
            "555555000000aaaaaaaaaaaaaaaaa8aaaaa8000000aaaaaaaaaaaa000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "5555550000002aaaaa2aaaaaaaaaaaaaaaaa000000aaaaaaaaaaaa000000aa2aaaaa28aaaa22aaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaa",
            "555555000000aaaa2aaaaa2aaaa8aaaaa8aa000000aa8aaaaa8aaa028a80a88a2aa88a2aa88a28a88a22a88a2aa88a2aa88a2aa88a2a",
            "555555005555aa0555aaa055aaaa1aaaaa15000001aaaaa1aaaaa10002a1aaa8a82aa8a8aaa8aaaaaa2aaaaa2aaaaa2aaaaa2aaaaa2a",
            "555555555555555555555555aaaa6a5555555555555555555555555555555555555aaaaa155555155555155555155aaa155555155555",
            "555555555555555555555555555555555555555555555555555555555555555555aaaa95555555555555555555aaaaaa555555555555",
            "5555555555555555555555555555555555555555555555555555555555555555555555555555555555545554025554a8555402555548",
            "555555555555555555555555555555555555555555555555555554554002554a2a554a2a554a2a014a28a840000000028aaaa8000000",
            "5555555555555555555555555555555555555555555555550006aaa28555a28555a28554a285020004a8000042aaaaa000000000028a",
            "555555555555555555555555555555555555555555555555aaaa55555555555555001555aa1aaa001555a15555015555005555a85555",
            "555555555555555555555555555555555555555555555555555555555555555555555555a55555555555555555555555555555555555",
            "5550aa5550aa5550005550005550aa5550aa5550805550aa5550aa555000555000555555555555555555555555555555555555555555",
            "aaaaaaaaaaaa000000000000aaaaaaaaaaaa000002aaaaaaaaaaaa000000000000555015555015555015555015555015555015555215",
            "aaaaaaaaaaaa000000000000aaaaaaaaaaaaa00000aaaaaaaaaaaa000000000000555555555555555555555555555555555555555555",
            "aaaaaaaaaaaa000000000000aaaaa8aaaaa8000022aaaa80aaaa8a00002a00002a555000550aaa54aaaa54aaaa52aaaa52aaaa4aaaaa",
            "8aa8a22aa8a22aa2a2aaa2a2aa8aa2aa8aa2aa8aa2002aa2a8aaa8a8aaaaa8aaaa000000a8aaaaa2aaa0000aa02a8aa02a8aa0000aa0",
            "a2aa8aa2aa8aa2aa8aa2aa8aa2aa8aa2aa8aa22a8aa2aa8a000000aaaaaaaaaaaa000000aaaaaaa0a00aa0aa0aa0a008a0aa0aa0aa0a",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa8aaaaa2aaaaaa000000aaaaaaaaaaaa000000aaaaaa82aaaa00aa020028a882a8a882aa02",
            "8a28aa8a28aa8a28aa8a28aa8a28aa8a28aa8a28aa8a28aa2a2a00aa2aaaaa2aaa000000aa2aaaaa2802aa282a02280202282aaa282a",
            "aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaa88aaaa28aaaaa8000002aaaaaaaaaaaa000000aaaaaa800a0a828a0a800a0a8aaa0a8aaa80",
            "a22aaaa22aaaa22aaaa22aaaa22aaaa22aaaa22aaaa22aaaa28000a2aaaaa2aaaa000000a2aaaa22a00222a0aa22a0aa22a0aaa2a0aa",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa8aaaaaa000000aaaaaaaaaaaa000000aaaaaa828a0a828a0a828a0a828a0a828a0a",
            "a2aaaaa2aaaaa2aaaaa2aaaaa2aaaa22aaa2a2aa8aa2aaaa000000aaaaaaaaaaaa000000aaaaaaa8000aaaa0aaaaa0aaaaa0aaaaa0aa",
            "228aaa228aaa228aaa228aaa228aaa228aaa228aaa228aaaa2a000a2aaaaa2aaaa000000a2aaaaa2a82aa2a82aa28002a28002a2a82a",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa000000aaaaaaaaaaaa000000aaaaaaa00200aa820aa0020aa0aa0aa0aa0a",
            "aa2aaaaa2aaaaa2aaaaa2aaaaa2aaaa22aaa8a2aaaaa2aaa000000aaaaaaaaaaaa000000aaaaaa2002aa20a2aa20a2a020a2a020a2aa",
            "a88a2aa88a2aa88a2aa88a2aa88a2aa88a2aa88a2aa88a2a028a80aa8aaaaa8aaa000000aa8aaa0a8a0a0a8a02008a00008a0a0a8a0a",
            "aaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaa22aaa8a2aaaaa2a0000aaaaaaaaaaaaaa000000aaaaaa20080220a8aa20a80220aa8220aa82",
            "155555155555855555855555855555855555856aaa855555855555855555855555015555a156aaa15555a15555a15555a15555a15555",
            "555555555555555555555555555555555555aaaaaa555555555555555555555555555555a55aaa555555555555555555555555555555",
            "555551555554555542555550555555555555555555555555555555555555555555555555aa5555555555555555555555555555555555",
            "42800228aaa880000294aaaa14aaa8540001555555555555555555555555555555555555555555555555555555555555555555555555",
            "aaa82aaaa840aaa854aaa85500006a555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "01555555aa95555555555555a9aa55555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555215555215555215555215555215555215555215555215555215555215555215555215555215555215555215555215555215555215",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "4aaaaa40aaaa4a00004a2aaa4a2aaa4a2aaa4a2aaa4a2aaa4a2aaa4a2aaa402aaa4aaaaa4aaaaa402aaa4a2aaa4a2aa8522aa8522aa8",
            "8aaaa08aaaaa0000008aaaaa2aaaaa2aaaaa2aaaaa2aaaaa0aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa002aaaaa2aaaaa2aaa",
            "a0a00aaaaaaa000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa000008aaaaa2aaaa8",
            "82aaaaaaaaaa000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2aaaa0a8aa000000aaaaaa020aaa",
            "aa282aaa2aaa000000aa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaa002aaaaa2aaaaa2aaa",
            "8aaaa2aaaaaa000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "a2a002a2aaaa000000a2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaa8a2aaa8a2aaa8a2aaa8a2aaa8a2aaa8a2aaaaa2aaaa",
            "800a00aaaaaa000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa00aaaa002aaa002aaaaa2aaaaa2aaaaa2aaa002aaaaaaaaaaaaaaa",
            "2aa0aaaaaaaa000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            "a2a82aa2aaaa000000a2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaaa",
            "a00200aaaaaa000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2aaaaaaa88aaaa02a2a",
            "2002aaaaaaaa000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa8aa8aaaaaaa",
            "0a8a0aaa8aaa000000aa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaaaa8aaa",
            "200802aaaaaa000000aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa8aaaaaaaaaaaaaaaaaaaa",
            "a15555a155550015552a15552a1aaa2a15552a15552a15552a15552a15552a15552a15552a15552a1555001555a15555a15555a156aa",
            "555555555555555555555555aa96aa555555555555555555555555555555555555555555555555555555555555555555555555aaaa5a",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555215555215555215555215555215555215555215555215555215555215555215555215555215555215555215555215555215555215",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "542aa854aaaa00002a00000a00000a00000a000000555550555555555555555555555555555555555555555555555555555555555555",
            "002aaa2aaaa82aaaa02aaaa82aaa222a08a200000a20820a0208aa000000000000500005555555555555555555555555555555555555",
            "2a0000a80000a00000a0000aa000aa800aaa802aa880aa8200aa2a02a8a002a2804aa2004a8a004a88084a80084aa0004aa0004aa008",
            "000000000000000000aaaa00aaaaa0aaaaaa0a82aaaaa82a000a8a0000aa00202aa0a20800008a0002a20a82202aa020aaa000aaa202",
            "022aaa002aaa002aaa002aa8002aa2002a88800a20a00800a00000a80000a84000aa10002a14002a15002a1555aa1555aa16aaa81555",
            "a2a2aaaa8aaa2a2aaaa82aaa20a8aa80aaaa028aa8022aaa088aaa20228a800aa8000000000000000000500001500001aaaaaa555555",
            "a2aaaaa2aaaaa2aaaaa2aaaaa2aaaaa2aaa8a2a8a2a2a20aa0880a2200282000aa000000000000000000555555555555a55555555555",
            "aaaaaaaaaaaa8aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa8aa2aaaaaa28aa282aa000000000000000000555555555555555555555555",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaa8aaaaaaaaaa88a8aa22a2a00288800a20002880000000000000000000540000540000555555555555",
            "a2aaaaa2aaa0a2a280a28a00a20800a0220080880000280000a80002a80202a01200014a00014a00054a01554a01554a55554a55554a",
            "000000000000000000000aaa00aaaa0aaaaa2aa80aaa82aaaa2a00a8a000a28000a200a08a000088080080080aa0002aa000aaa008aa",
            "000002000000000000aa0000aaa000aaaa0082aa80a82aa00a8aa000aaa8202aa8a208aa008a2a02a22a82202aa020aaa000aaa202a8",
            "aa8aaaaa8aaaaa8aaa288aaa028aa80288800a020208080000000840000040000015540015555515555515aaaa15555515555516aaa9",
            "aaaaa8a2a2a2aaaa8a2a2a2a20a82a82a0aa0882aa220a00000000000055000000000000555555555555a96555555555555555555555",
            "a15555a15555800555000555000555000555000555000555000555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555215555215555215555215555215555215555215555215555215555215555215400000555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555000000555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555000000555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555000000555555555555555555555555555555555555",
            "4aa8a84aaa204aa20042a28242a8a048aa2a48aaaa480aaa4840aa48500a485500000000555555555555555555555555555555555555",
            "2a828200000aa0a00a80202aa800a2000a8aaaa82aa802a8aaaaa0aaaa00000005000000555555555555555555555555555555555555",
            "a81555a81555a81aaaa05555a055558155a1015a060550a81a0a026040a9002800000000555555555555555555555555555555555555",
            "55555555555595a65555555515555555555595555521aaa5955555555555455555000000555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555595555555555555555555555000000555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555000000555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555000000555555555555555555555555555555555555",
            "55554a55554a55554a555552555552555554555554555555555555555555555555000000555555555555555555555555555555555555",
            "a8a82aaa2000a200a0a28280a8a0a8aa2a00aaaaaa0aaaa840aaaa500aaa550000000000555555555555555555555555555555555555",
            "8282a8000aa8a00aa8202aa000a2a00a8a81a82a0102a808aaa022aa0088000620000000555555555555555555555555555555555555",
            "1555551555551955555155495565555a8955a209aa889155228955022a55008095000000555555555555555555555555555555555555",
            "555555555555555555555555555555555555aaaa95555555255555955555555555000000555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555005555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555",
            "555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555"
        };

        for (int i = 0; i < tileData.length; i++) {
            int[] tile = decodeTileHex(tileData[i]);
            characters.set(160 + i, tile);
        }
    }

    private static int[] decodeTileHex(String hex) {
        int[] pixels = new int[216];
        int pi = 0;

        for (int i = 0; i < hex.length(); i += 2) {
            int b = Integer.parseInt(hex.substring(i, i + 2), 16);

            for (int j = 0; j < 4 && pi < pixels.length; j++) {
                pixels[pi++] = b >> 6 - j * 2 & 3;
            }
        }

        return pixels;
    }
}
