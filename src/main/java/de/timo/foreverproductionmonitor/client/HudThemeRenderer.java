package de.timo.foreverproductionmonitor.client;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Compact HUD-specific visual renderer.
 *
 * The full monitor and the mini HUD share the same theme identities, but this
 * renderer deliberately interprets each theme for a 188px always-visible HUD.
 * Decorations are kept small and quiet; the theme identity lives in the frame,
 * mark and a few controlled ambient details.
 */
final class HudThemeRenderer {
    private static final int WIDTH = 188;

    private HudThemeRenderer() {
    }

    static Colors colors(ClientConfig.InterfaceStyle style) {
        return switch (style) {
            case STANDARD -> new Colors(0xFF080A0D, 0xFF4F5968, 0xFF14181F, 0xFF202833, 0xFF191F28, 0xFF161B22, 0xFF6E7C90, 0xFF9AA8BA, 0xFFF0F2F5);
            case FOREVER -> new Colors(0xFF09111A, 0xFF365366, 0xFF0D1620, 0xFF172432, 0xFF111C28, 0xFF0D1620, 0xFF55D6FF, 0xFFB66BFF, 0xFFF2F7FC);
            case AE2 -> new Colors(0xFF111318, 0xFF626973, 0xFF1F2329, 0xFF30353D, 0xFF252930, 0xFF1F2329, 0xFFBCC2CA, 0xFFE2E5E9, 0xFFF5F6F7);
            case ORITECH -> new Colors(0xFF0D1113, 0xFF4B5B60, 0xFF161B1E, 0xFF252D31, 0xFF171E21, 0xFF13191C, 0xFFFF7A2D, 0xFF45DCE6, 0xFFF2F4F5);
            case MEKANISM -> new Colors(0xFF101A1E, 0xFF49656B, 0xFF132127, 0xFF21333A, 0xFF18262C, 0xFF132127, 0xFF5AD1D8, 0xFF6EDB9D, 0xFFE9F4F5);
            case QUANTUM -> new Colors(0xFF060409, 0xFF5B4165, 0xFF15121A, 0xFF241D2C, 0xFF1D1724, 0xFF18131E, 0xFF8C43D6, 0xFFD39DFF, 0xFFF1ECF6);
            case HOLOGRAPHIC -> new Colors(0xFF031014, 0xFF196573, 0xFF071A20, 0xFF0A2931, 0xFF09232A, 0xFF071E24, 0xFF35E5FF, 0xFF8AF6FF, 0xFFE9FEFF);
            case MONOCHROME -> new Colors(0xFF050805, 0xFF4C654B, 0xFF0C120C, 0xFF132013, 0xFF101A10, 0xFF0D160D, 0xFF91B58C, 0xFFBED3BA, 0xFFE6F0E3);
            case MINIMAL -> new Colors(0xFF090A0D, 0xFF333841, 0xFF111318, 0xFF171A20, 0xFF14171C, 0xFF121419, 0xFF707782, 0xFF999FA8, 0xFFE8EAED);
            case CARBON -> new Colors(0xFF050506, 0xFF3B3E42, 0xFF0C0D0F, 0xFF151619, 0xFF111214, 0xFF0E0F11, 0xFF70757B, 0xFF9B9FA5, 0xFFE9EAEC);
            case TERMINAL -> new Colors(0xFF020603, 0xFF1C5428, 0xFF061008, 0xFF0A1A0D, 0xFF081509, 0xFF061107, 0xFF3DDC64, 0xFF8AF39E, 0xFFD9FFE1);
            case DEEP_SPACE -> new Colors(0xFF020309, 0xFF2F3D67, 0xFF080A16, 0xFF0F1530, 0xFF0C1126, 0xFF090E20, 0xFF7D8FFF, 0xFFD29BFF, 0xFFF0F2FF);
            case COPPER -> new Colors(0xFF100906, 0xFF75513D, 0xFF1C110C, 0xFF2B1C14, 0xFF24170F, 0xFF1F130D, 0xFFC87B4B, 0xFF65A393, 0xFFF3DDD0);
            case AURORA -> new Colors(0xFF050A12, 0xFF315D72, 0xFF0A1520, 0xFF102233, 0xFF0E1D2C, 0xFF0B1926, 0xFF56D7BD, 0xFFA775E8, 0xFFF0FBFF);
            case REDSTONE -> new Colors(0xFF0D0505, 0xFF68282A, 0xFF180B0B, 0xFF281010, 0xFF211010, 0xFF1B0D0D, 0xFFE13A36, 0xFFFF8A45, 0xFFFFEAE2);
            case FROST -> new Colors(0xFF071018, 0xFF4D718A, 0xFF0E1A23, 0xFF162A37, 0xFF12232E, 0xFF0F1D27, 0xFF7DCAEE, 0xFFD2F0FF, 0xFFF4FBFF);
            case NATURE -> new Colors(0xFF040804, 0xFF31502E, 0xFF0A130A, 0xFF102010, 0xFF0D1A0D, 0xFF0A160A, 0xFF4E9847, 0xFFD278A4, 0xFFE3EAD6);
            case CAT -> new Colors(0xFF102228, 0xFF527D81, 0xFF152B31, 0xFF1D3940, 0xFF26434A, 0xFF20383E, 0xFFFFAD96, 0xFFFFD49F, 0xFFFFF4E5);
            case CUSTOM -> customColors();
        };
    }

    static void renderFrame(GuiGraphics g, int x, int y, int height, int opacity,
                            ClientConfig.InterfaceStyle theme, ClientConfig.HudLayoutStyle layout) {
        Colors c = colors(theme);
        int panelAlpha = clampAlpha(opacity);
        int headerAlpha = Math.min(255, opacity + 20);
        int frameAlpha = Math.min(255, opacity + 45);

        if (layout == ClientConfig.HudLayoutStyle.NONE) {
            g.fill(x, y, x + WIDTH, y + height, withAlpha(c.panel(), panelAlpha));
            g.fill(x, y, x + WIDTH, y + 18, withAlpha(c.header(), headerAlpha));
            decorateFrame(g, x, y, height, opacity, theme, layout, c);
            return;
        }

        int expand = layout == ClientConfig.HudLayoutStyle.FULL ? 3 : 1;
        g.fill(x - expand, y - expand, x + WIDTH + expand, y + height + expand, withAlpha(c.outer(), frameAlpha));
        if (layout == ClientConfig.HudLayoutStyle.FULL) {
            g.fill(x - 2, y - 2, x + WIDTH + 2, y + height + 2, withAlpha(c.border(), frameAlpha));
            g.fill(x - 1, y - 1, x + WIDTH + 1, y + height + 1, withAlpha(c.outer(), frameAlpha));
        }
        g.fill(x, y, x + WIDTH, y + height, withAlpha(c.panel(), panelAlpha));
        g.fill(x, y, x + WIDTH, y + 18, withAlpha(c.header(), headerAlpha));

        if (layout == ClientConfig.HudLayoutStyle.MINIMAL) {
            g.fill(x + 4, y + 17, x + WIDTH - 4, y + 18, withAlpha(c.accentA(), Math.min(130, frameAlpha)));
        }

        decorateFrame(g, x, y, height, opacity, theme, layout, c);
    }

    static void renderRow(GuiGraphics g, int x, int y, int row, int opacity,
                          ClientConfig.InterfaceStyle theme, ClientConfig.HudLayoutStyle layout) {
        Colors c = colors(theme);
        int alpha = Math.min(255, opacity + 5);
        int left = layout == ClientConfig.HudLayoutStyle.NONE ? x : x + 1;
        int right = layout == ClientConfig.HudLayoutStyle.NONE ? x + WIDTH : x + WIDTH - 1;
        g.fill(left, y, right, y + 17, withAlpha((row & 1) == 0 ? c.rowEven() : c.rowOdd(), alpha));

        // Every theme gets a small row-level identity cue. These stay near the
        // edges so names, icons and values remain untouched even at compact HUD sizes.
        switch (theme) {
            case STANDARD -> {
                g.fill(x + 2, y + 15, x + 13, y + 16, withAlpha(c.border(), 40));
                float pulse = 0.5f + 0.5f * (float)Math.sin((phase(9600L, 0.18f) + row * 0.13f) * Math.PI * 2.0);
                g.fill(x + WIDTH - 7, y + 7, x + WIDTH - 5, y + 9,
                        withAlpha(c.accentB(), 30 + Math.round(pulse * 34.0f)));
            }
            case FOREVER -> {
                int[] systems = {0xFFC9D3DD, 0xFF55D6C7, 0xFFFF8A3D, 0xFFB66BFF};
                int system = systems[Math.floorMod(row, systems.length)];
                g.fill(x + 2, y + 3, x + 4, y + 14, withAlpha(system, 58));
                g.fill(x + WIDTH - 16, y + 5, x + WIDTH - 5, y + 12, withAlpha(0xFF0A141D, 96));
                g.fill(x + WIDTH - 13, y + 7, x + WIDTH - 9, y + 10, withAlpha(system, 54));
                float packet = (phase(8400L, 0.24f) + row * 0.17f) % 1.0f;
                int px = x + WIDTH - 32 + Math.round(packet * 12.0f);
                g.fill(px, y + 14, px + 3, y + 15, withAlpha(c.accentA(), motionAlpha(18, 42)));
            }
            case AE2 -> {
                int trace = controllerColor(row * 0.07f, Math.min(64, alpha));
                g.fill(x + 2, y + 3, x + 3, y + 14, trace);
                g.fill(x + 3, y + 13, x + 13, y + 14, trace);
                int mx = x + WIDTH - 13;
                int my = y + 4;
                g.fill(mx, my, mx + 9, my + 9, withAlpha(0xFF24272D, 104));
                g.fill(mx + 2, my + 2, mx + 7, my + 7,
                        controllerColor(0.12f + row * 0.05f, motionAlpha(38, 76)));
            }
            case ORITECH -> {
                g.fill(x + 2, y + 3, x + 4, y + 14, withAlpha(c.accentA(), 78));
                g.fill(x + WIDTH - 12, y + 4, x + WIDTH - 4, y + 13, withAlpha(0xFF10171A, 110));
                float energy = 0.5f + 0.5f * (float)Math.sin((phase(8800L, 0.22f) + row * 0.11f) * Math.PI * 2.0);
                g.fill(x + WIDTH - 10, y + 6, x + WIDTH - 6, y + 11,
                        withAlpha(c.accentB(), 44 + Math.round(energy * 38.0f)));
            }
            case MEKANISM -> {
                // Small configuration port + energy gauge, matching Mekanism's machine UI.
                int port = (row & 1) == 0 ? 0xFF4E8BD8 : 0xFFD85858;
                g.fill(x + 2, y + 5, x + 5, y + 11, withAlpha(port, 62));
                g.fill(x + WIDTH - 10, y + 4, x + WIDTH - 5, y + 13, withAlpha(c.border(), 58));
                float energy = 0.5f + 0.5f * (float)Math.sin((phase(9200L, 0.28f) + row * 0.13f) * Math.PI * 2.0);
                int fill = 2 + Math.round(energy * 5.0f);
                g.fill(x + WIDTH - 8, y + 12 - fill, x + WIDTH - 7, y + 12,
                        withAlpha(c.accentB(), 56));
                int px = x + WIDTH - 25 + Math.round(phase(7600L, 0.25f) * 10.0f);
                g.fill(px, y + 14, px + 3, y + 15, withAlpha(c.accentA(), motionAlpha(18, 40)));
            }
            case QUANTUM -> {
                // One contained compute cell; no detached linework across the row.
                int qx = x + WIDTH - 16;
                int qy = y + 3;
                g.fill(qx, qy, qx + 12, qy + 11, withAlpha(0xFF17151C, 104));
                g.fill(qx + 2, qy + 2, qx + 10, qy + 9, withAlpha(0xFF3A1B58, 76));
                float pulse = 0.5f + 0.5f * (float)Math.sin((phase(10200L, 0.31f) + row * 0.09f) * Math.PI * 2.0);
                g.fill(qx + 4, qy + 4, qx + 8, qy + 7,
                        withAlpha(c.accentB(), 36 + Math.round(pulse * 50.0f)));
                g.fill(qx + 2, qy + 10, qx + 10, qy + 11, withAlpha(0xFFD2A62B, 34));
            }
            case HOLOGRAPHIC -> {
                corner(g, x + 3, y + 3, true, true, withAlpha(c.accentA(), 34));
                float scan = phase(6800L, 0.34f);
                int sx = x + 8 + Math.round(scan * (WIDTH - 22));
                g.fill(sx, y + 14, Math.min(x + WIDTH - 4, sx + 7), y + 15,
                        withAlpha(c.accentB(), motionAlpha(20, 52)));
            }
            case MONOCHROME -> {
                g.fill(x + 3, y + 15, x + WIDTH - 3, y + 16, withAlpha(c.titleText(), 12));
                float scan = phase(13800L, 0.27f);
                int sx = x + 4 + Math.round(scan * (WIDTH - 24));
                g.fill(sx, y + 15, sx + 14, y + 16, withAlpha(c.accentA(), motionAlpha(18, 36)));
            }
            case MINIMAL -> {
                int span = 16 + ((row & 1) == 0 ? 8 : 0);
                int center = x + WIDTH / 2;
                g.fill(center - span / 2, y + 15, center + span / 2, y + 16, withAlpha(c.accentA(), 30));
                g.fill(center, y + 14, center + 1, y + 16, withAlpha(c.accentB(), 44));
            }
            case CARBON -> {
                rivet(g, x + 4, y + 7, withAlpha(c.accentA(), 46));
                for (int px = x + WIDTH - 22; px < x + WIDTH - 5; px += 5) {
                    g.fill(px, y + 5, px + 2, y + 12, withAlpha(c.border(), 26));
                }
                float conveyor = phase(9200L, 0.2f);
                int cx = x + WIDTH - 22 + Math.round(conveyor * 13.0f);
                g.fill(cx, y + 13, cx + 3, y + 14, withAlpha(c.accentA(), motionAlpha(20, 48)));
            }
            case TERMINAL -> {
                g.fill(x + 2, y + 3, x + 3, y + 14, withAlpha(c.accentA(), 38));
                int prompt = withAlpha(c.accentB(), 52);
                g.fill(x + WIDTH - 13, y + 6, x + WIDTH - 11, y + 8, prompt);
                g.fill(x + WIDTH - 11, y + 8, x + WIDTH - 9, y + 10, prompt);
                boolean cursor = !motionEnabled() || phase(1150L, 0.2f) < 0.58f;
                if (cursor) g.fill(x + WIDTH - 8, y + 10, x + WIDTH - 4, y + 11, withAlpha(c.accentA(), 68));
            }
            case DEEP_SPACE -> {
                int sx = x + WIDTH - 8 - mod(hash(3100 + row * 47), 10);
                int sy = y + 4 + mod(hash(4700 + row * 61), 8);
                float twinkle = 0.5f + 0.5f * (float)Math.sin((phase(12400L, 0.17f) + row * 0.19f) * Math.PI * 2.0);
                star(g, sx, sy, withAlpha((row % 3) == 0 ? c.accentB() : c.titleText(),
                        28 + Math.round(twinkle * 42.0f)));
            }
            case COPPER -> {
                rivet(g, x + 4, y + 6, withAlpha(c.accentA(), 64));
                rivet(g, x + WIDTH - 7, y + 9, withAlpha(c.accentB(), 54));
                g.fill(x + WIDTH - 18, y + 14, x + WIDTH - 9, y + 15,
                        withAlpha((row & 1) == 0 ? c.accentA() : c.accentB(), 28));
            }
            case AURORA -> {
                float flow = phase(15000L, 0.21f) + row * 0.08f;
                int y1 = y + 13 + (int)Math.round(Math.sin(flow * Math.PI * 2.0) * 1.5);
                int y2 = y + 10 + (int)Math.round(Math.sin(flow * Math.PI * 2.0 + 1.8) * 1.5);
                g.fill(x + WIDTH - 25, y1, x + WIDTH - 14, y1 + 1, withAlpha(c.accentA(), 30));
                g.fill(x + WIDTH - 17, y2, x + WIDTH - 5, y2 + 1, withAlpha(c.accentB(), 28));
            }
            case REDSTONE -> {
                float power = 0.5f + 0.5f * (float)Math.sin((phase(7200L, 0.24f) + row * 0.12f) * Math.PI * 2.0);
                int dust = withAlpha(c.accentA(), 34 + Math.round(power * 42.0f));
                g.fill(x + 2, y + 8, x + 11, y + 9, dust);
                g.fill(x + 10, y + 6, x + 11, y + 11, dust);
                g.fill(x + WIDTH - 8, y + 7, x + WIDTH - 5, y + 10,
                        withAlpha(c.accentB(), 32 + Math.round(power * 48.0f)));
            }
            case FROST -> {
                g.fill(x + 2, y + 15, x + 12, y + 16, withAlpha(c.accentB(), 32));
                if ((row & 1) == 0) {
                    snowflake(g, x + WIDTH - 7, y + 7, withAlpha(c.accentB(), 38));
                } else {
                    iceCorner(g, x + WIDTH - 4, y + 13, withAlpha(c.accentA(), 24));
                }
            }
            case NATURE -> {
                int leaf = withAlpha(c.accentA(), 64);
                g.fill(x + 2, y + 3, x + 3, y + 14, withAlpha(c.accentA(), 42));
                drawLeaf(g, x + 5, y + 6 + (row & 1) * 4, leaf, (row & 1) == 0);
                if (row % 3 == 0) {
                    flower(g, x + WIDTH - 8, y + 8, withAlpha(c.accentB(), 34), withAlpha(c.titleText(), 28));
                }
            }
            case CAT -> {
                g.fill(x + 1, y + 2, x + 2, y + 15,
                        withAlpha((row & 1) == 0 ? c.accentA() : c.accentB(), 62));
                float step = phase(9800L, 0.22f);
                int px = x + WIDTH - 10 - Math.round((step + row * 0.13f - (float)Math.floor(step + row * 0.13f)) * 13.0f);
                drawPaw(g, px, y + 8, withAlpha((row & 1) == 0 ? c.accentB() : c.accentA(), motionAlpha(24, 44)));
            }
            case CUSTOM -> {
                g.fill(x + 2, y + 3, x + 3, y + 14, withAlpha((row & 1) == 0 ? c.accentA() : c.accentB(), 54));
                g.fill(x + WIDTH - 10, y + 5, x + WIDTH - 7, y + 12, withAlpha(c.accentA(), 38));
                g.fill(x + WIDTH - 6, y + 5, x + WIDTH - 3, y + 12, withAlpha(c.accentB(), 38));
            }
        }
    }

    static void renderMark(GuiGraphics g, int x, int y, int opacity,
                           ClientConfig.InterfaceStyle theme, ClientConfig.HudLayoutStyle layout) {
        if (layout == ClientConfig.HudLayoutStyle.NONE || layout == ClientConfig.HudLayoutStyle.MINIMAL) return;
        Colors c = colors(theme);
        int a = Math.min(255, opacity + 65);
        int cx = x + 9;
        int cy = y + 9;

        switch (theme) {
            case AE2 -> {
                g.fill(cx - 6, cy - 6, cx + 7, cy + 7, withAlpha(0xFF626973, a / 2));
                g.fill(cx - 4, cy - 4, cx + 5, cy + 5, withAlpha(0xFF24272D, a));
                g.fill(cx - 2, cy - 2, cx + 3, cy + 3, controllerColor(0.05f, a));
                g.fill(cx + 3, cy - 4, cx + 5, cy - 2, controllerColor(0.22f, a / 2));
            }
            case ORITECH -> {
                g.fill(cx - 5, cy - 5, cx + 6, cy + 6, withAlpha(0xFF11181B, a));
                g.fill(cx - 5, cy - 5, cx - 3, cy + 6, withAlpha(c.accentA(), a));
                g.fill(cx - 2, cy - 3, cx + 4, cy + 4, withAlpha(c.border(), a));
                g.fill(cx, cy - 1, cx + 3, cy + 2, withAlpha(c.accentB(), a));
            }
            case MEKANISM -> {
                g.fill(cx - 5, cy - 5, cx - 1, cy + 5, withAlpha(c.border(), a));
                g.fill(cx - 4, cy - 1, cx - 2, cy + 4, withAlpha(c.accentA(), a));
                g.fill(cx + 1, cy - 5, cx + 5, cy + 5, withAlpha(c.border(), a));
                g.fill(cx + 2, cy - 3, cx + 4, cy + 4, withAlpha(c.accentB(), a));
                g.fill(cx - 1, cy, cx + 2, cy + 1, withAlpha(c.titleText(), a / 2));
            }
            case QUANTUM -> {
                g.fill(cx - 6, cy - 5, cx + 7, cy + 6, withAlpha(0xFF777981, a / 2));
                g.fill(cx - 4, cy - 4, cx + 5, cy + 5, withAlpha(0xFF17151C, a));
                g.fill(cx - 2, cy - 2, cx + 3, cy + 3, withAlpha(0xFF3A1B58, a));
                g.fill(cx - 1, cy - 1, cx + 2, cy + 2, withAlpha(c.accentB(), a));
                g.fill(cx - 3, cy + 4, cx + 4, cy + 5, withAlpha(0xFFD2A62B, a / 2));
            }
            case HOLOGRAPHIC -> {
                corner(g, cx - 5, cy - 5, true, true, withAlpha(c.accentA(), a));
                corner(g, cx + 5, cy + 5, false, false, withAlpha(c.accentB(), a));
            }
            case MONOCHROME -> {
                g.fill(cx - 5, cy, cx + 6, cy + 1, withAlpha(c.accentA(), a));
                g.fill(cx, cy - 5, cx + 1, cy + 6, withAlpha(c.accentA(), a / 2));
            }
            case MINIMAL -> { }
            case CARBON -> {
                diagonal(g, cx - 4, cy - 4, cx + 4, cy + 4, withAlpha(c.accentA(), a / 2));
                diagonal(g, cx - 4, cy + 4, cx + 4, cy - 4, withAlpha(c.accentB(), a / 2));
            }
            case TERMINAL -> {
                g.fill(cx - 5, cy - 4, cx - 3, cy + 4, withAlpha(c.accentA(), a));
                g.fill(cx - 1, cy + 2, cx + 5, cy + 4, withAlpha(c.accentB(), a));
            }
            case DEEP_SPACE -> {
                star(g, cx - 2, cy - 1, withAlpha(c.accentB(), a));
                star(g, cx + 4, cy - 4, withAlpha(c.titleText(), a / 2));
            }
            case COPPER -> {
                rivet(g, cx - 4, cy - 4, withAlpha(c.accentA(), a));
                rivet(g, cx + 3, cy + 3, withAlpha(c.accentB(), a));
            }
            case AURORA -> {
                g.fill(cx - 5, cy - 2, cx + 1, cy, withAlpha(c.accentA(), a));
                g.fill(cx, cy, cx + 6, cy + 2, withAlpha(c.accentB(), a));
            }
            case REDSTONE -> {
                g.fill(cx - 5, cy, cx + 3, cy + 1, withAlpha(c.accentA(), a));
                g.fill(cx + 2, cy - 2, cx + 5, cy + 3, withAlpha(c.accentB(), a));
            }
            case FROST -> snowflake(g, cx, cy, withAlpha(c.accentB(), a));
            case NATURE -> {
                g.fill(cx, cy - 5, cx + 1, cy + 5, withAlpha(c.accentA(), a));
                drawLeaf(g, cx + 3, cy - 2, withAlpha(c.accentA(), a), true);
                drawLeaf(g, cx - 2, cy + 2, withAlpha(c.accentA(), a), false);
                flower(g, cx + 1, cy - 5, withAlpha(c.accentB(), a), withAlpha(c.titleText(), a));
            }
            case CAT -> drawCatFace(g, cx, cy, c, a);
            case FOREVER -> {
                // Central network core with four subsystem nodes.
                g.fill(cx - 3, cy - 3, cx + 4, cy + 4, withAlpha(0xFF0A141D, a));
                g.fill(cx - 1, cy - 1, cx + 2, cy + 2, withAlpha(c.accentA(), a));
                g.fill(cx - 6, cy - 1, cx - 4, cy + 1, withAlpha(0xFFC9D3DD, a));
                g.fill(cx + 5, cy - 1, cx + 7, cy + 1, withAlpha(0xFFFF8A3D, a));
                g.fill(cx - 1, cy - 6, cx + 1, cy - 4, withAlpha(0xFF55D6C7, a));
                g.fill(cx - 1, cy + 5, cx + 1, cy + 7, withAlpha(0xFFB66BFF, a));
            }
            case STANDARD -> {
                g.fill(cx - 4, cy - 4, cx + 5, cy + 5, withAlpha(c.accentA(), a / 2));
                g.fill(cx - 2, cy - 2, cx + 3, cy + 3, withAlpha(c.panel(), a));
            }
            case CUSTOM -> {
                g.fill(cx - 5, cy - 5, cx + 2, cy + 2, withAlpha(c.accentA(), a));
                g.fill(cx - 1, cy - 1, cx + 6, cy + 6, withAlpha(c.accentB(), a));
            }
        }
    }

    private static void decorateFrame(GuiGraphics g, int x, int y, int h, int opacity,
                                      ClientConfig.InterfaceStyle theme, ClientConfig.HudLayoutStyle layout, Colors c) {
        int a = Math.min(190, opacity + 35);
        switch (theme) {
            case STANDARD -> {
                g.fill(x + 4, y + 17, x + WIDTH - 4, y + 18, withAlpha(c.accentA(), 54));
                float travel = phase(16000L, 0.2f);
                if (motionEnabled()) {
                    g.pose().pushPose();
                    g.pose().translate(x + 10 + travel * 150.0f, y, 0.0f);
                    g.fill(0, 2, 12, 3, withAlpha(c.accentB(), motionAlpha(20, 48)));
                    g.pose().popPose();
                }
                float status = 0.5f + 0.5f * (float)Math.sin(travel * Math.PI * 2.0);
                g.fill(x + WIDTH - 9, y + 7, x + WIDTH - 6, y + 10,
                        withAlpha(c.accentB(), 30 + Math.round(status * 34.0f)));
            }
            case FOREVER -> {
                g.fill(x, y, x + WIDTH, y + 2, withAlpha(c.accentA(), 88));
                g.fill(x + 4, y + h - 4, x + WIDTH - 4, y + h - 3, withAlpha(c.border(), 42));
                int[] systems = {0xFFC9D3DD, 0xFF55D6C7, 0xFFFF8A3D, 0xFFB66BFF};
                int busY = y + h - 4;
                for (int i = 0; i < 4; ++i) {
                    int nx = x + 30 + i * 40;
                    g.fill(nx - 4, busY - 5, nx + 5, busY + 2, withAlpha(0xFF0A141D, 96));
                    g.fill(nx - 2, busY - 3, nx + 3, busY, withAlpha(systems[i], 58));
                }
                float packet = phase(8800L, 0.27f);
                int px = x + 10 + Math.round(packet * 164.0f);
                g.fill(px, busY - 1, Math.min(x + WIDTH - 5, px + 4), busY + 2,
                        withAlpha(c.accentA(), motionAlpha(24, 58)));
                // Tiny paired quantum link in the header.
                g.fill(x + WIDTH - 40, y + 7, x + WIDTH - 34, y + 13, withAlpha(0xFF1B1722, 94));
                g.fill(x + WIDTH - 18, y + 7, x + WIDTH - 12, y + 13, withAlpha(0xFF1B1722, 94));
                g.fill(x + WIDTH - 34, y + 9, x + WIDTH - 18, y + 10, withAlpha(0xFFB66BFF, 32));
            }
            case AE2 -> {
                g.fill(x, y, x + WIDTH, y + 2, withAlpha(0xFFE2E5E9, 90));
                g.fill(x + 2, y + 2, x + 4, y + h - 3, withAlpha(0xFF626973, 64));
                // Three visible ME Controller cells in the header.
                for (int i = 0; i < 3; ++i) {
                    int mx = x + WIDTH - 46 + i * 13;
                    g.fill(mx, y + 4, mx + 11, y + 15, withAlpha(0xFF24272D, 112));
                    g.fill(mx + 2, y + 6, mx + 9, y + 13,
                            controllerColor(i * 0.13f, motionAlpha(40, 76)));
                }
                controllerTrace(g, x + 18, y + h - 5, 48, -8, controllerColor(0.31f, 40), false);
            }
            case ORITECH -> {
                // Compact Oritech machine frame: orange structure, cyan powered glass,
                // and one small travelling gantry carriage instead of repeated dashes.
                g.fill(x, y, x + WIDTH, y + 2, withAlpha(c.border(), 88));
                g.fill(x + 2, y + 2, x + 4, y + h - 3, withAlpha(c.accentA(), 82));
                g.fill(x + 4, y + 3, x + WIDTH - 17, y + 4, withAlpha(c.border(), 54));

                int moduleX = x + WIDTH - 15;
                g.fill(moduleX, y + 4, x + WIDTH - 4, y + 15, withAlpha(0xFF10171A, 120));
                g.fill(moduleX + 3, y + 7, x + WIDTH - 7, y + 12,
                        withAlpha(c.accentB(), motionAlpha(48, 82)));

                float travel = phase(12000L, 0.28f);
                float pingPong = 0.5f - 0.5f * (float)Math.cos(travel * Math.PI * 2.0);
                int carriageX = x + 18 + Math.round(pingPong * 82.0f);
                g.fill(carriageX - 4, y + 4, carriageX + 5, y + 10, withAlpha(0xFF11181B, 112));
                g.fill(carriageX - 2, y + 6, carriageX + 3, y + 9,
                        withAlpha(c.accentB(), motionAlpha(46, 78)));
                g.fill(carriageX, y + 9, carriageX + 1, y + 15, withAlpha(c.border(), 58));

                rivet(g, x + 7, y + h - 7, withAlpha(c.border(), 68));
                rivet(g, x + WIDTH - 8, y + h - 7, withAlpha(c.border(), 68));
            }
            case MEKANISM -> {
                g.fill(x, y, x + WIDTH, y + 2, withAlpha(c.border(), 76));
                // Chemical tank at left.
                float chem = 0.42f + 0.26f * (0.5f + 0.5f * (float)Math.sin(phase(15000L, 0.3f) * Math.PI * 2.0));
                tank(g, x + 5, y + 4, 7, 12, chem, withAlpha(c.accentA(), 58), withAlpha(c.border(), 72));
                // Energy gauge at right.
                g.fill(x + WIDTH - 11, y + 4, x + WIDTH - 5, y + 15, withAlpha(c.border(), 68));
                float power = 0.55f + 0.32f * (0.5f + 0.5f * (float)Math.sin(phase(11000L, 0.2f) * Math.PI * 2.0));
                int fill = Math.max(2, Math.round(8.0f * power));
                g.fill(x + WIDTH - 9, y + 13 - fill, x + WIDTH - 7, y + 13, withAlpha(c.accentB(), 68));
                // Mekanism side configuration tabs.
                g.fill(x + 15, y + 5, x + 18, y + 9, withAlpha(0xFF4E8BD8, 66));
                g.fill(x + 15, y + 10, x + 18, y + 14, withAlpha(0xFFD85858, 62));
            }
            case QUANTUM -> {
                g.fill(x, y, x + WIDTH, y + 2, withAlpha(0xFF777981, 72));
                // One coherent mini compute bank in the upper-right.
                int baseX = x + WIDTH - 46;
                int activeCell = Math.min(2, (int)(phase(8800L, 0.56f) * 3.0f));
                for (int i = 0; i < 3; ++i) {
                    int mx = baseX + i * 13;
                    g.fill(mx, y + 4, mx + 11, y + 15, withAlpha(0xFF17151C, 112));
                    g.fill(mx + 2, y + 6, mx + 9, y + 13, withAlpha(0xFF3A1B58, 68));
                    int glow = i == activeCell ? motionAlpha(54, 94) : 30;
                    g.fill(mx + 4, y + 8, mx + 7, y + 11, withAlpha(c.accentB(), glow));
                }
                g.fill(baseX + 2, y + 15, baseX + 37, y + 16, withAlpha(0xFFD2A62B, 40));
            }
            case HOLOGRAPHIC -> {
                for (int sy = y + 4; sy < y + h - 3; sy += 6) {
                    g.fill(x + 3, sy, x + WIDTH - 3, sy + 1, withAlpha(c.accentA(), 10));
                }
                if (motionEnabled()) {
                    float scan = phase(7000L, 0.4f);
                    g.pose().pushPose();
                    g.pose().translate(0.0f, y + scan * Math.max(1, h - 2), 0.0f);
                    g.fill(x + 3, 0, x + WIDTH - 3, 1, withAlpha(c.accentB(), motionAlpha(18, 48)));
                    g.pose().popPose();
                }
            }
            case MONOCHROME -> {
                for (int sy = y + 5; sy < y + h - 3; sy += 5) g.fill(x + 3, sy, x + WIDTH - 3, sy + 1, withAlpha(c.titleText(), 6));
                if (motionEnabled()) {
                    float scan = phase(14000L, 0.4f);
                    g.pose().pushPose();
                    g.pose().translate(0.0f, y + scan * Math.max(1, h - 4), 0.0f);
                    g.fill(x + 4, 0, x + WIDTH - 4, 1, withAlpha(c.accentA(), motionAlpha(8, 24)));
                    g.pose().popPose();
                }
            }
            case MINIMAL -> {
                float breathe = 0.5f + 0.5f * (float)Math.sin(phase(14000L, 0.22f) * Math.PI * 2.0);
                int half = 44 + Math.round(breathe * 26.0f);
                int center = x + WIDTH / 2;
                g.fill(center - half, y + 17, center + half, y + 18, withAlpha(c.accentA(), 24 + Math.round(breathe * 16.0f)));
                g.fill(center, y + 4, center + 1, y + 8, withAlpha(c.accentB(), 28));
            }
            case CARBON -> {
                for (int px = x + 10; px < x + WIDTH - 10; px += 24) {
                    g.fill(px, y + 4, px + 10, y + 5, withAlpha(c.border(), 24));
                    g.fill(px + 7, y + h - 5, px + 17, y + h - 4, withAlpha(c.accentA(), 18));
                }
                if (motionEnabled()) {
                    float sheen = phase(22000L, 0.22f);
                    g.pose().pushPose();
                    g.pose().translate(x - 20 + sheen * (WIDTH + 40), y, 0.0f);
                    diagonal(g, 0, 3, 24, h - 3, withAlpha(c.titleText(), motionAlpha(3, 14)));
                    g.pose().popPose();
                }
            }
            case TERMINAL -> {
                g.fill(x + 3, y + 17, x + WIDTH - 3, y + 18, withAlpha(c.accentA(), 48));
                boolean cursor = !motionEnabled() || phase(1200L, 0.2f) < 0.55f;
                if (cursor) g.fill(x + WIDTH - 11, y + 6, x + WIDTH - 5, y + 8, withAlpha(c.accentB(), 76));
            }
            case DEEP_SPACE -> {
                for (int i = 0; i < 7; ++i) {
                    int sx = x + 14 + mod(hash(2100 + i * 71), 160);
                    int sy = y + 5 + mod(hash(3300 + i * 53), Math.max(8, h - 10));
                    int twinkle = 28 + Math.round((0.5f + 0.5f * (float)Math.sin(phase(11000L + i * 700L, i * 0.1f) * Math.PI * 2.0)) * 34.0f);
                    star(g, sx, sy, withAlpha(i % 3 == 0 ? c.accentB() : c.titleText(), twinkle));
                }
            }
            case COPPER -> {
                rivet(g, x + 5, y + 5, withAlpha(c.accentA(), 90));
                rivet(g, x + WIDTH - 7, y + h - 7, withAlpha(c.accentB(), 76));
                if (motionEnabled()) {
                    float travel = 0.5f + 0.5f * (float)Math.sin(phase(22000L, 0.2f) * Math.PI * 2.0);
                    g.pose().pushPose();
                    g.pose().translate(x + 20 + travel * 120.0f, y, 0.0f);
                    g.fill(-10, 3, 10, h - 3, withAlpha(c.accentA(), motionAlpha(4, 17)));
                    g.pose().popPose();
                }
            }
            case AURORA -> {
                float flow = phase(19000L, 0.18f);
                for (int px = x + 4; px < x + WIDTH - 4; px += 5) {
                    double u = (px - x) / (double)WIDTH;
                    int py1 = y + 7 + (int)Math.round(Math.sin(u * 5.4 + flow * Math.PI * 2.0) * 2.0);
                    int py2 = y + 12 + (int)Math.round(Math.sin(u * 4.2 - flow * Math.PI * 1.5) * 2.0);
                    g.fill(px, py1, px + 6, py1 + 1, withAlpha(c.accentA(), 24));
                    g.fill(px, py2, px + 6, py2 + 1, withAlpha(c.accentB(), 20));
                }
            }
            case REDSTONE -> {
                float power = 0.5f + 0.5f * (float)Math.sin(phase(9000L, 0.2f) * Math.PI * 2.0);
                int dust = withAlpha(c.accentA(), 36 + Math.round(power * 48));
                g.fill(x + 6, y + h - 6, x + 72, y + h - 5, dust);
                g.fill(x + 72, y + h - 12, x + 73, y + h - 5, dust);
                g.fill(x + 72, y + h - 12, x + 94, y + h - 11, dust);
                g.fill(x + 92, y + h - 14, x + 97, y + h - 9, withAlpha(c.accentB(), 38 + Math.round(power * 52)));
            }
            case FROST -> {
                iceCorner(g, x + 2, y + 2, withAlpha(c.accentB(), 52));
                iceCorner(g, x + WIDTH - 3, y + h - 3, withAlpha(c.accentA(), 44));
                if (motionEnabled()) {
                    for (int i = 0; i < 4; ++i) {
                        float fall = phase(12000L + i * 1900L, i * 0.2f);
                        float sx = x + 24 + mod(hash(7000 + i * 67), 140);
                        float sy = y - 3 + fall * (h + 6);
                        g.pose().pushPose();
                        g.pose().translate(sx, sy, 0.0f);
                        snowflake(g, 0, 0, withAlpha(c.titleText(), 30 + i * 5));
                        g.pose().popPose();
                    }
                }
            }
            case NATURE -> natureFrame(g, x, y, h, c);
            case CAT -> catFrame(g, x, y, h, c);
            case CUSTOM -> {
                g.fill(x, y, x + 112, y + 2, withAlpha(c.accentA(), 86));
                g.fill(x + 112, y, x + WIDTH, y + 2, withAlpha(c.accentB(), 86));
                corner(g, x + 2, y + 2, true, true, withAlpha(c.accentA(), 48));
                corner(g, x + WIDTH - 2, y + h - 2, false, false, withAlpha(c.accentB(), 48));
                float glide = phase(12600L, 0.35f);
                int gx = x + 8 + Math.round(glide * (WIDTH - 24));
                g.fill(gx, y + h - 3, Math.min(x + WIDTH - 4, gx + 10), y + h - 2,
                        withAlpha(glide < 0.58f ? c.accentA() : c.accentB(), motionAlpha(18, 42)));
            }
        }
    }

    private static void catFrame(GuiGraphics g, int x, int y, int h, Colors c) {
        g.fill(x + 2, y + 1, x + WIDTH - 2, y + 3, withAlpha(c.accentB(), 190));
        g.fill(x + 3, y + 17, x + WIDTH - 3, y + 18, withAlpha(c.accentA(), 130));
        g.fill(x + 3, y + h - 3, x + WIDTH - 3, y + h - 2, withAlpha(c.accentB(), 76));
        // An edge-bound glint moves smoothly without passing through HUD labels.
        if (motionEnabled()) {
            float glide = (1.0f - (float)Math.cos(phase(10500L, 0.0f) * Math.PI * 2.0)) * 0.5f;
            g.pose().pushPose();
            g.pose().translate(x + 20 + glide * (WIDTH - 54), y + 2, 0.0f);
            g.fill(0, 0, 16, 1, withAlpha(c.titleText(), motionAlpha(36, 88)));
            g.pose().popPose();
        }
        g.fill(x + WIDTH - 14, y + 7, x + WIDTH - 8, y + 8, withAlpha(c.accentB(), 130));
        g.fill(x + WIDTH - 12, y + 5, x + WIDTH - 11, y + 11, withAlpha(c.titleText(), 115));
    }

    private static void natureFrame(GuiGraphics g, int x, int y, int h, Colors c) {
        float vineGrowth = growthCycle(phase(30000L, 0.76f));
        int maxStem = Math.max(18, h - 12);

        g.pose().pushPose();
        g.pose().translate(x + 4, y + 5, 0.0f);
        g.pose().scale(1.0f, Math.max(0.001f, vineGrowth), 1.0f);
        g.fill(0, 0, 1, maxStem, withAlpha(c.accentA(), 62));
        g.pose().popPose();

        for (int i = 0; i < 5; ++i) {
            float threshold = (8 + i * Math.max(8, maxStem / 5)) / (float)Math.max(1, maxStem);
            float leafProgress = smoothstep(clamp01((vineGrowth - threshold) / 0.16f));
            if (leafProgress <= 0.001f) continue;
            int ly = y + 8 + i * Math.max(8, maxStem / 5);
            boolean right = (i & 1) == 0;
            float sway = motionEnabled() ? (float)Math.sin((phase(15000L, 0.3f) + i * 0.12f) * Math.PI * 2.0) * 1.2f : 0.0f;
            g.pose().pushPose();
            g.pose().translate(x + 4 + (right ? 4 + sway : -2 - sway), ly, 0.0f);
            g.pose().scale(leafProgress, leafProgress, 1.0f);
            drawLeaf(g, 0, 0, withAlpha(c.accentA(), Math.round(76 * leafProgress)), right);
            g.pose().popPose();

            if ((i == 2 || i == 4) && leafProgress > 0.72f) {
                float bloom = smoothstep((leafProgress - 0.72f) / 0.28f);
                g.pose().pushPose();
                g.pose().translate(x + 8 + (right ? 3 : -3), ly - 3, 0.0f);
                g.pose().scale(bloom, bloom, 1.0f);
                flower(g, 0, 0, withAlpha(c.accentB(), 82), withAlpha(c.titleText(), 74));
                g.pose().popPose();
            }
        }

        // Tiny tree grows independently at the opposite lower corner.
        if (h > 60) {
            float tree = growthCycle(phase(52000L, 0.18f));
            int tx = x + WIDTH - 15;
            int base = y + h - 5;
            int trunkH = Math.min(24, Math.max(12, h / 4));
            float trunk = smoothstep(clamp01(tree / 0.48f));
            g.pose().pushPose();
            g.pose().translate(tx, base, 0.0f);
            g.pose().scale(1.0f, Math.max(0.001f, trunk), 1.0f);
            g.fill(-1, -trunkH, 2, 0, withAlpha(0xFF6A4728, 70));
            g.pose().popPose();

            float crown = smoothstep(clamp01((tree - 0.42f) / 0.32f));
            if (crown > 0.001f) {
                float sway = motionEnabled() ? (float)Math.sin(phase(18000L, 0.2f) * Math.PI * 2.0) * 1.0f : 0.0f;
                g.pose().pushPose();
                g.pose().translate(tx + sway, base - trunkH, 0.0f);
                g.pose().scale(crown, crown, 1.0f);
                leafCluster(g, 0, 0, 6, withAlpha(c.accentA(), 66));
                leafCluster(g, -5, 3, 4, withAlpha(c.accentA(), 54));
                leafCluster(g, 5, 3, 4, withAlpha(c.accentA(), 58));
                if (tree > 0.78f) flower(g, 4, -2, withAlpha(c.accentB(), 72), withAlpha(c.titleText(), 58));
                g.pose().popPose();
            }
        }

        g.fill(x + 6, y + h - 4, x + WIDTH - 7, y + h - 3, withAlpha(c.accentA(), 24));
    }

    private static Colors customColors() {
        float hue = ClientConfig.effectiveHudCustomHue() / 359.0f;
        int a = hsv(hue, 0.72f, 0.95f);
        int b = hsv((hue + 0.11f) % 1.0f, 0.62f, 0.90f);
        return new Colors(0xFF06070A, darken(a, 0.48f), darken(a, 0.12f), darken(a, 0.20f),
                darken(a, 0.16f), darken(a, 0.13f), a, b, 0xFFF2F3F5);
    }

    private static int controllerColor(float offset, int opacity) {
        float hue = phase(18000L, 0.075f) + offset;
        hue -= (float)Math.floor(hue);
        return withAlpha(hsv(hue, 0.86f, 0.96f), opacity);
    }

    private static void controllerTrace(GuiGraphics g, int sx, int sy, int dx, int dy, int color, boolean mirror) {
        int ex = sx + dx;
        int ey = sy + dy;
        int mx = sx + dx / 2;
        int my = sy + dy / 2;
        if (!mirror) {
            horizontal(g, sx, mx, sy, color);
            vertical(g, mx, sy, my, color);
            horizontal(g, mx, ex, my, color);
            vertical(g, ex, my, ey, color);
        } else {
            vertical(g, sx, sy, my, color);
            horizontal(g, sx, mx, my, color);
            vertical(g, mx, my, ey, color);
            horizontal(g, mx, ex, ey, color);
        }
    }

    private static void horizontal(GuiGraphics g, int x1, int x2, int y, int color) {
        g.fill(Math.min(x1, x2), y, Math.max(x1, x2) + 1, y + 1, color);
    }

    private static void vertical(GuiGraphics g, int x, int y1, int y2, int color) {
        g.fill(x, Math.min(y1, y2), x + 1, Math.max(y1, y2) + 1, color);
    }

    private static void gear(GuiGraphics g, int cx, int cy, int radius, int body, int accent) {
        int r = Math.max(3, radius);
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0.0f);
        g.pose().mulPose(Axis.ZP.rotationDegrees(phase(14000L, 0.1f) * 360.0f));
        g.fill(-r, -1, r + 1, 2, accent);
        g.fill(-1, -r, 2, r + 1, accent);
        g.fill(-r + 2, -r + 2, r - 1, r - 1, body);
        g.pose().popPose();
    }

    private static void tank(GuiGraphics g, int x, int y, int w, int h, float level, int fluid, int border) {
        g.fill(x, y, x + w, y + h, border);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, withAlpha(0xFF081114, 96));
        float clamped = clamp01(level);
        g.pose().pushPose();
        g.pose().translate(0.0f, y + h - 2, 0.0f);
        g.pose().scale(1.0f, Math.max(0.001f, clamped), 1.0f);
        g.fill(x + 2, -(h - 4), x + w - 2, 0, fluid);
        g.pose().popPose();
    }

    private static void iceCorner(GuiGraphics g, int x, int y, int color) {
        g.fill(x, y, x + 12, y + 1, color);
        g.fill(x, y, x + 1, y + 12, color);
        diagonal(g, x + 4, y, x, y + 4, withAlpha(color, Math.max(16, (color >>> 24) - 12)));
    }

    private static void snowflake(GuiGraphics g, int x, int y, int color) {
        g.fill(x - 1, y, x + 2, y + 1, color);
        g.fill(x, y - 1, x + 1, y + 2, color);
    }

    private static void drawPaw(GuiGraphics g, int cx, int cy, int color) {
        g.fill(cx - 2, cy + 1, cx + 3, cy + 4, color);
        g.fill(cx - 4, cy - 2, cx - 2, cy, color);
        g.fill(cx - 1, cy - 4, cx + 1, cy - 2, color);
        g.fill(cx + 2, cy - 2, cx + 4, cy, color);
    }

    private static void drawEar(GuiGraphics g, int x, int y, boolean left, int outer, int inner) {
        int d = left ? 1 : -1;
        g.fill(Math.min(x, x + d * 7), y + 5, Math.max(x, x + d * 7) + 1, y + 7, outer);
        g.fill(Math.min(x + d, x + d * 6), y + 3, Math.max(x + d, x + d * 6) + 1, y + 5, outer);
        g.fill(Math.min(x + d * 2, x + d * 5), y + 1, Math.max(x + d * 2, x + d * 5) + 1, y + 3, outer);
        g.fill(Math.min(x + d * 3, x + d * 4), y + 1, Math.max(x + d * 3, x + d * 4) + 1, y + 3, inner);
    }

    private static void drawCatFace(GuiGraphics g, int cx, int cy, Colors c, int alpha) {
        int outline = withAlpha(0xFF173238, alpha);
        int fur = withAlpha(c.titleText(), alpha);
        int coral = withAlpha(c.accentA(), alpha);
        int apricot = withAlpha(c.accentB(), Math.max(40, alpha - 16));
        int eye = withAlpha(0xFF102329, alpha);

        float cycle = phase(7600L, 0.24f);
        boolean blink = cycle > 0.91f && cycle < 0.955f;
        boolean twitch = motionEnabled() && cycle > 0.62f && cycle < 0.69f;

        // Pointed feline ears. The previous rectangular ears plus the coloured
        // face patch read like a cow at HUD scale, so the silhouette now does
        // most of the work before any facial detail is added.
        int leftTipY = cy - 9 + (twitch ? 1 : 0);
        g.fill(cx - 6, leftTipY, cx - 4, cy - 7 + (twitch ? 1 : 0), outline);
        g.fill(cx - 7, cy - 7 + (twitch ? 1 : 0), cx - 3, cy - 4, outline);
        g.fill(cx + 4, cy - 9, cx + 6, cy - 7, outline);
        g.fill(cx + 3, cy - 7, cx + 7, cy - 4, outline);

        // Cat head: slightly wider at the cheeks, narrower at the forehead.
        g.fill(cx - 6, cy - 5, cx + 7, cy + 5, outline);
        g.fill(cx - 5, cy - 4, cx + 6, cy + 4, fur);
        g.fill(cx - 4, cy + 3, cx + 5, cy + 6, fur);

        // Small triangular ear interiors, no large facial patch.
        g.fill(cx - 5, cy - 7 + (twitch ? 1 : 0), cx - 4, cy - 5, coral);
        g.fill(cx + 4, cy - 7, cx + 5, cy - 5, coral);

        // A subtle warm forehead tuft ties the icon to the current cat palette
        // without turning into a cow-like spot.
        g.fill(cx, cy - 4, cx + 2, cy - 2, apricot);

        // Feline eyes are narrow/slanted rather than square.
        if (blink) {
            g.fill(cx - 4, cy - 1, cx - 1, cy, eye);
            g.fill(cx + 2, cy - 1, cx + 5, cy, eye);
        } else {
            g.fill(cx - 4, cy - 2, cx - 1, cy, eye);
            g.fill(cx + 2, cy - 2, cx + 5, cy, eye);
            g.fill(cx - 3, cy - 2, cx - 2, cy - 1, withAlpha(c.accentB(), alpha));
            g.fill(cx + 3, cy - 2, cx + 4, cy - 1, withAlpha(c.accentB(), alpha));
        }

        // Small cat muzzle, nose and mouth.
        g.fill(cx - 2, cy + 1, cx + 4, cy + 4, withAlpha(c.titleText(), Math.max(40, alpha - 8)));
        g.fill(cx, cy + 1, cx + 2, cy + 2, coral);
        g.fill(cx, cy + 2, cx + 1, cy + 4, eye);
        g.fill(cx - 1, cy + 4, cx, cy + 5, eye);
        g.fill(cx + 1, cy + 4, cx + 2, cy + 5, eye);

        // Long whiskers are the clearest cat cue at this tiny pixel size.
        int whisker = withAlpha(c.titleText(), alpha / 2);
        diagonal(g, cx - 3, cy + 1, cx - 9, cy, whisker);
        diagonal(g, cx - 3, cy + 3, cx - 9, cy + 4, whisker);
        diagonal(g, cx + 4, cy + 1, cx + 10, cy, whisker);
        diagonal(g, cx + 4, cy + 3, cx + 10, cy + 4, whisker);
    }

    private static void drawTail(GuiGraphics g, int sx, int sy, int length, int amplitude, float turn, int color) {
        int prevX = sx;
        int prevY = sy;
        for (int i = 2; i <= length; i += 3) {
            int px = sx + i;
            int py = sy - (int)Math.round(Math.sin((i / (double)length) * Math.PI * 1.35 + turn * Math.PI * 2.0)
                    * amplitude * (i / (double)length));
            diagonal(g, prevX, prevY, px, py, color);
            prevX = px;
            prevY = py;
        }
    }

    private static void yarnBall(GuiGraphics g, int cx, int cy, Colors c, float turn) {
        int body = withAlpha(c.accentA(), 70);
        int thread = withAlpha(c.accentB(), 72);
        g.fill(cx - 4, cy - 3, cx + 5, cy + 4, body);
        g.fill(cx - 3, cy - 4, cx + 4, cy + 5, body);
        int o = Math.round((float)Math.sin(turn * Math.PI * 2.0) * 2.0f);
        diagonal(g, cx - 3, cy - 2 + o, cx + 3, cy + 2 + o, thread);
        diagonal(g, cx - 3, cy + 2 - o, cx + 3, cy - 2 - o, withAlpha(c.titleText(), 38));
    }

    private static void drawHeart(GuiGraphics g, int cx, int cy, int color) {
        g.fill(cx - 2, cy - 1, cx, cy + 1, color);
        g.fill(cx + 1, cy - 1, cx + 3, cy + 1, color);
        g.fill(cx - 1, cy, cx + 2, cy + 3, color);
        g.fill(cx, cy + 3, cx + 1, cy + 4, color);
    }

    private static void drawLeaf(GuiGraphics g, int x, int y, int color, boolean right) {
        if (right) {
            g.fill(x, y, x + 4, y + 2, color);
            g.fill(x + 1, y - 1, x + 3, y + 3, color);
        } else {
            g.fill(x - 3, y, x + 1, y + 2, color);
            g.fill(x - 2, y - 1, x, y + 3, color);
        }
    }

    private static void flower(GuiGraphics g, int x, int y, int petal, int center) {
        g.fill(x - 1, y - 3, x + 2, y, petal);
        g.fill(x - 1, y + 1, x + 2, y + 4, petal);
        g.fill(x - 3, y - 1, x, y + 2, petal);
        g.fill(x + 1, y - 1, x + 4, y + 2, petal);
        g.fill(x, y, x + 1, y + 1, center);
    }

    private static void leafCluster(GuiGraphics g, int x, int y, int r, int color) {
        g.fill(x - r, y - r / 2, x + r + 1, y + r / 2 + 1, color);
        g.fill(x - r / 2, y - r, x + r / 2 + 1, y + r + 1, withAlpha(color, Math.max(20, (color >>> 24) - 8)));
    }

    private static void corner(GuiGraphics g, int x, int y, boolean left, boolean top, int color) {
        int dx = left ? 1 : -1;
        int dy = top ? 1 : -1;
        g.fill(Math.min(x, x + dx * 6), y, Math.max(x, x + dx * 6) + 1, y + 1, color);
        g.fill(x, Math.min(y, y + dy * 6), x + 1, Math.max(y, y + dy * 6) + 1, color);
    }

    private static void rivet(GuiGraphics g, int x, int y, int color) {
        g.fill(x, y, x + 2, y + 2, withAlpha(0xFF1E2025, 160));
        g.fill(x, y, x + 1, y + 1, color);
    }

    private static void star(GuiGraphics g, int x, int y, int color) {
        g.fill(x, y, x + 1, y + 1, color);
        if ((color >>> 24) > 60) {
            g.fill(x - 1, y, x + 2, y + 1, withAlpha(color, Math.max(12, (color >>> 24) / 2)));
        }
    }

    private static void diagonal(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1;
        int y = y1;
        while (true) {
            g.fill(x, y, x + 1, y + 1, color);
            if (x == x2 && y == y2) break;
            int e2 = err * 2;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
    }

    private static float growthCycle(float p) {
        float v = clamp01(p);
        if (v < 0.58f) return v / 0.58f;
        if (v < 0.86f) return 1.0f;
        return Math.max(0.0f, 1.0f - (v - 0.86f) / 0.14f);
    }

    private static float smoothstep(float v) {
        float x = clamp01(v);
        return x * x * (3.0f - 2.0f * x);
    }

    private static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static float phase(long durationMillis, float fallback) {
        if (!motionEnabled() || durationMillis <= 0L) return fallback;
        long nanos = durationMillis * 1_000_000L;
        return (float)((double)Math.floorMod(System.nanoTime(), nanos) / (double)nanos);
    }

    private static boolean motionEnabled() {
        return (Boolean)ClientConfig.VALUES.guiAnimations.get()
                && (Boolean)ClientConfig.VALUES.hudAnimations.get()
                && (Double)ClientConfig.VALUES.hudAnimationIntensity.get() > 0.0;
    }

    private static int motionAlpha(int low, int high) {
        if (!motionEnabled()) return low;
        float intensity = ((Double)ClientConfig.VALUES.hudAnimationIntensity.get()).floatValue();
        return Math.round(low + (high - low) * intensity);
    }

    private static int hsv(float h, float s, float v) {
        float hue = h - (float)Math.floor(h);
        float scaled = hue * 6.0f;
        int sector = (int)Math.floor(scaled);
        float f = scaled - sector;
        float p = v * (1.0f - s);
        float q = v * (1.0f - s * f);
        float t = v * (1.0f - s * (1.0f - f));
        float rr, gg, bb;
        switch (Math.floorMod(sector, 6)) {
            case 0 -> { rr = v; gg = t; bb = p; }
            case 1 -> { rr = q; gg = v; bb = p; }
            case 2 -> { rr = p; gg = v; bb = t; }
            case 3 -> { rr = p; gg = q; bb = v; }
            case 4 -> { rr = t; gg = p; bb = v; }
            default -> { rr = v; gg = p; bb = q; }
        }
        return 0xFF000000
                | Math.round(rr * 255.0f) << 16
                | Math.round(gg * 255.0f) << 8
                | Math.round(bb * 255.0f);
    }

    private static int darken(int color, float amount) {
        float keep = 1.0f - clamp01(amount);
        int r = Math.round(((color >> 16) & 0xFF) * keep);
        int g = Math.round(((color >> 8) & 0xFF) * keep);
        int b = Math.round((color & 0xFF) * keep);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | clampAlpha(alpha) << 24;
    }

    private static int clampAlpha(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }

    private static int hash(int value) {
        int x = value;
        x ^= x >>> 16;
        x *= 0x7feb352d;
        x ^= x >>> 15;
        x *= 0x846ca68b;
        x ^= x >>> 16;
        return x;
    }

    private static int mod(int value, int divisor) {
        return Math.floorMod(value, Math.max(1, divisor));
    }

    record Colors(int outer, int border, int panel, int header, int rowEven, int rowOdd,
                  int accentA, int accentB, int titleText) {
    }
}
