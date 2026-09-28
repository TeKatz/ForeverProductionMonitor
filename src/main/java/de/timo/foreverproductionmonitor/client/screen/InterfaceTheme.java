/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.GuiGraphics
 */
package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;

public final class InterfaceTheme {
    private InterfaceTheme() {
    }

    public static Palette current() {
        ClientConfig.InterfaceStyle style =
                (ClientConfig.InterfaceStyle)((Object)ClientConfig.VALUES.interfaceStyle.get());
        Palette palette;
        if (style == ClientConfig.InterfaceStyle.CUSTOM) {
            palette = InterfaceTheme.customPalette(
                    (Integer)ClientConfig.VALUES.interfaceCustomHue.get(),
                    (Integer)ClientConfig.VALUES.interfaceCustomSecondaryHue.get(),
                    (Double)ClientConfig.VALUES.interfaceCustomBrightness.get(),
                    (Double)ClientConfig.VALUES.interfaceCustomPrimarySaturation.get(),
                    (Double)ClientConfig.VALUES.interfaceCustomSecondarySaturation.get(),
                    (Double)ClientConfig.VALUES.interfaceCustomSurfaceSaturation.get(),
                    (Double)ClientConfig.VALUES.interfaceCustomContrast.get(),
                    (Double)ClientConfig.VALUES.interfaceCustomHeaderStrength.get(),
                    (Double)ClientConfig.VALUES.interfaceCustomBorderStrength.get(),
                    (Double)ClientConfig.VALUES.interfaceCustomAccentBrightness.get());
        } else {
            palette = InterfaceTheme.palette(style,
                    (Integer)ClientConfig.VALUES.interfaceCustomHue.get(),
                    (Integer)ClientConfig.VALUES.interfaceCustomSecondaryHue.get(),
                    (Double)ClientConfig.VALUES.interfaceCustomBrightness.get());
        }
        return palette.withOpacity((Double)ClientConfig.VALUES.interfaceOpacity.get());
    }

    public static Palette palette(ClientConfig.InterfaceStyle interfaceStyle, int n) {
        return InterfaceTheme.palette(interfaceStyle, n, (n + 38) % 360, 0.52);
    }

    public static Palette palette(ClientConfig.InterfaceStyle interfaceStyle, int n, int n2, double d) {
        return switch (interfaceStyle) {
            default -> throw new IncompatibleClassChangeError();
            case ClientConfig.InterfaceStyle.STANDARD -> new Palette(1510542095, -2063597568, -16118768, -233037784, -14078408, -15723751, -13617599, -14670804, -15065563, 1716344420, -14209995, -986379, -7103576, -8484972, -4406324, -9932418);
            case ClientConfig.InterfaceStyle.FOREVER -> new Palette(0x72030810, 0x85000000, 0xFF09111A, 0xF1111C29, 0xFF1B2937, 0xFF081019, 0xFF233446, 0xFF111C28, 0xFF0D1620, 0x6655D6FF, 0xFF172432, 0xFFF2F7FC, 0xFF91A7B8, 0xFF55D6FF, 0xFFB66BFF, 0xFF365366);
            case ClientConfig.InterfaceStyle.AE2 -> new Palette(0x7006080B, 0x85000000, 0xFF1B1E23, 0xF1282C33, 0xFF373C44, 0xFF101216, 0xFF444A54, 0xFF252930, 0xFF1F2329, 0x66C9CCD3, 0xFF30353D, 0xFFF5F6F7, 0xFFA8ADB5, 0xFFBCC2CA, 0xFFE2E5E9, 0xFF626973);
            case ClientConfig.InterfaceStyle.ORITECH -> new Palette(0x68100704, 0x85000000, 0xFF0D1113, 0xF1161B1E, 0xFF252D31, 0xFF0D1214, 0xFF28363B, 0xFF171E21, 0xFF13191C, 0x6645DCE6, 0xFF1F282C, 0xFFF2F4F5, 0xFFA8B2B5, 0xFFFF7A2D, 0xFF45DCE6, 0xFF4B5B60);
            case ClientConfig.InterfaceStyle.MEKANISM -> new Palette(0x6E061014, 0x85000000, 0xFF101A1E, 0xF11B2A30, 0xFF2A3D45, 0xFF0C171B, 0xFF31474F, 0xFF18262C, 0xFF132127, 0x665AD1D8, 0xFF21333A, 0xFFE9F4F5, 0xFF91A6AA, 0xFF5AD1D8, 0xFF6EDB9D, 0xFF49656B);
            case ClientConfig.InterfaceStyle.QUANTUM -> new Palette(0x76040308, -2063597568, 0xFF17151C, 0xF11D1A23, 0xFF2A2631, 0xFF0E0C12, 0xFF322D3A, 0xFF201C27, 0xFF18151E, 0x665C2B83, 0xFF28222F, 0xFFF1ECF6, 0xFFA39DA9, 0xFF8C43D6, 0xFFD39DFF, 0xFF54415F);
            case ClientConfig.InterfaceStyle.HOLOGRAPHIC -> new Palette(1207962642, 0x65000000, -16576233, -653254616, -652592314, -653978341, -534754215, -921423816, -921885652, 1885140223, -652658108, -1442049, -7617847, -11671041, -6264321, -13009782);
            case ClientConfig.InterfaceStyle.MONOCHROME -> new Palette(0x58000000, Integer.MIN_VALUE, -16250614, -233169889, -14013135, -15921391, -13091775, -14539479, -15000031, 1717528174, -13947085, -723724, -6578525, -2631204, -8551800, -10788762);
            case ClientConfig.InterfaceStyle.MINIMAL -> new Palette(0x39000000, 0x45000000, 0, -401139168, -400744151, -653389546, -400085963, -652533978, -652862944, 1429881164, -400480722, -855051, -7169628, -4866873, -9142903, -12564654);
            case ClientConfig.InterfaceStyle.CARBON -> new Palette(0x70000000, -2063597568, 0xFF111315, 0xF11A1D20, 0xFF22262A, 0xFF0D0F11, 0xFF292E33, 0xFF191C1F, 0xFF14171A, 0x663C444B, 0xFF202429, 0xFFE8EDF2, 0xFF89939C, 0xFFE0A43A, 0xFF687681, 0xFF343A40);
            case ClientConfig.InterfaceStyle.TERMINAL -> new Palette(0x78000200, -2063597568, 0xFF001A08, 0xF008160C, 0xFF092B16, 0xFF031008, 0xFF0B351B, 0xFF071D0F, 0xFF05170C, 0x6638FF79, 0xFF0A2815, 0xFFD5FFE0, 0xFF63A978, 0xFF35F06D, 0xFFB1FF4C, 0xFF167536);
            case ClientConfig.InterfaceStyle.DEEP_SPACE -> new Palette(0x7A020618, -2063597568, 0xFF080A24, 0xF10D1230, 0xFF151B46, 0xFF07091C, 0xFF1A2152, 0xFF101536, 0xFF0B102A, 0x666D72FF, 0xFF151A43, 0xFFE9EBFF, 0xFF8A91C8, 0xFF6574FF, 0xFFFF61D8, 0xFF303A85);
            case ClientConfig.InterfaceStyle.COPPER -> new Palette(0x70100804, -2063597568, 0xFF2A1710, 0xF1342118, 0xFF4A2C1D, 0xFF21120C, 0xFF5B3521, 0xFF382218, 0xFF2E1A12, 0x66E58B4D, 0xFF47291B, 0xFFFFE7D2, 0xFFB79780, 0xFFE88947, 0xFF59B8A9, 0xFF815038);
            case ClientConfig.InterfaceStyle.AURORA -> new Palette(0x72020B18, -2063597568, 0xFF071B27, 0xF10A2730, 0xFF103A42, 0xFF061820, 0xFF124B50, 0xFF0B3036, 0xFF08262D, 0x6656FFD7, 0xFF0D3B40, 0xFFE3FFF8, 0xFF75B8AE, 0xFF38E6C1, 0xFFC55CFF, 0xFF27776E);
            case ClientConfig.InterfaceStyle.REDSTONE -> new Palette(0x74100000, -2063597568, 0xFF270909, 0xF1321010, 0xFF461414, 0xFF1B0707, 0xFF5A1919, 0xFF361010, 0xFF2A0B0B, 0x66FF3434, 0xFF431313, 0xFFFFE7E2, 0xFFC4877E, 0xFFFF3B30, 0xFFFFA126, 0xFF7D2420);
            case ClientConfig.InterfaceStyle.FROST -> new Palette(0x68101B28, -2063597568, 0xFF182C3D, 0xEE274154, 0xFF31566E, 0xFF112331, 0xFF3B6580, 0xFF263F51, 0xFF1D3546, 0x668DEBFF, 0xFF2C4D62, 0xFFF4FDFF, 0xFF9EC3D0, 0xFF7DDBF2, 0xFFD7F7FF, 0xFF56849B);
            case ClientConfig.InterfaceStyle.NATURE -> new Palette(0x7A040804, -2063597568, 0xFF0A120A, 0xF10D160C, 0xFF162316, 0xFF081008, 0xFF1B2E19, 0xFF101B0F, 0xFF0C160C, 0x66527F45, 0xFF172617, 0xFFE3EAD6, 0xFF89957A, 0xFF4E9847, 0xFFD278A4, 0xFF30432D);
            case ClientConfig.InterfaceStyle.CAT -> new Palette(0x86101C21, 0xB0000000, 0xFF152B31, 0xFF1D3940, 0xFF437078, 0xFF102228, 0xFF527D81, 0xFF26434A, 0xFF20383E, 0x66FFD49F, 0xFF31535A, 0xFFFFF4E5, 0xFFBAD1CC, 0xFFFFAD96, 0xFFFFD49F, 0xFF6A9693);
            case ClientConfig.InterfaceStyle.CUSTOM -> InterfaceTheme.customPalette(n, n2, d);
        };
    }

    private static Palette customPalette(int primaryHue, int secondaryHue, double brightness) {
        return InterfaceTheme.customPalette(primaryHue, secondaryHue, brightness,
                0.62, 0.52, 0.25, 1.0, 1.0, 1.0, 1.0);
    }

    private static Palette customPalette(int primaryHue, int secondaryHue, double brightness,
                                         double primarySaturation, double secondarySaturation,
                                         double surfaceSaturation, double contrast,
                                         double headerStrength, double borderStrength,
                                         double accentBrightness) {
        float primary = (float)Math.floorMod(primaryHue, 360) / 360.0f;
        float secondary = (float)Math.floorMod(secondaryHue, 360) / 360.0f;
        float base = (float)Math.max(0.2, Math.min(0.9, brightness));
        float primarySat = (float)Math.max(0.0, Math.min(1.0, primarySaturation));
        float secondarySat = (float)Math.max(0.0, Math.min(1.0, secondarySaturation));
        float surfaceSat = (float)Math.max(0.0, Math.min(0.75, surfaceSaturation));
        float contrastValue = (float)Math.max(0.65, Math.min(1.45, contrast));
        float headerValue = (float)Math.max(0.65, Math.min(1.45, headerStrength));
        float borderValue = (float)Math.max(0.5, Math.min(1.5, borderStrength));
        float accentValue = (float)Math.max(0.65, Math.min(1.25, accentBrightness));

        float panelV = InterfaceTheme.clamp01(base * 0.43f);
        float darkFactor = 1.0f / contrastValue;
        float brightFactor = contrastValue;
        int panel = 0xFF000000 | InterfaceTheme.hsv(primary, surfaceSat,
                InterfaceTheme.clamp01(panelV));
        int outer = 0xFF000000 | InterfaceTheme.hsv(primary, surfaceSat * 0.88f,
                InterfaceTheme.clamp01(panelV * 0.72f * darkFactor));
        int header = 0xFF000000 | InterfaceTheme.hsv(primary, surfaceSat * 0.92f,
                InterfaceTheme.clamp01(panelV * 1.34f * headerValue * brightFactor));
        int tableOuter = 0xFF000000 | InterfaceTheme.hsv(primary, surfaceSat * 0.82f,
                InterfaceTheme.clamp01(panelV * 0.64f * darkFactor));
        int tableHeader = 0xFF000000 | InterfaceTheme.hsv(primary, surfaceSat,
                InterfaceTheme.clamp01(panelV * 1.13f * headerValue * brightFactor));
        int rowEven = 0xFF000000 | InterfaceTheme.hsv(primary, surfaceSat * 0.74f,
                InterfaceTheme.clamp01(panelV * 0.84f));
        int rowOdd = 0xFF000000 | InterfaceTheme.hsv(primary, surfaceSat * 0.68f,
                InterfaceTheme.clamp01(panelV * 0.70f * darkFactor));
        int summary = 0xFF000000 | InterfaceTheme.hsv(primary, surfaceSat * 0.88f,
                InterfaceTheme.clamp01(panelV * 0.98f));
        int border = 0xFF000000 | InterfaceTheme.hsv(primary, Math.min(1.0f, surfaceSat + 0.12f),
                InterfaceTheme.clamp01(base * 0.78f * borderValue * brightFactor));

        float primaryAccentV = InterfaceTheme.clamp01((base + 0.40f) * accentValue);
        float secondaryAccentV = InterfaceTheme.clamp01((base + 0.48f) * accentValue);
        int accentA = 0xFF000000 | InterfaceTheme.hsv(primary, primarySat, primaryAccentV);
        int accentB = 0xFF000000 | InterfaceTheme.hsv(secondary, secondarySat, secondaryAccentV);
        int text = 0xFF000000 | InterfaceTheme.hsv(primary, surfaceSat * 0.10f,
                InterfaceTheme.clamp01(0.90f + 0.06f * brightFactor));
        int muted = 0xFF000000 | InterfaceTheme.hsv(primary, surfaceSat * 0.34f,
                InterfaceTheme.clamp01(0.50f + base * 0.22f));
        int hover = 0x66000000 | accentA & 0xFFFFFF;

        return new Palette(
                0x5A06080B,
                0x85000000,
                outer,
                0xF1000000 | panel & 0xFFFFFF,
                header,
                tableOuter,
                tableHeader,
                rowEven,
                rowOdd,
                hover,
                summary,
                text,
                muted,
                accentA,
                accentB,
                border);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    public static void drawPanel(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, ClientConfig.InterfaceStyle interfaceStyle, Palette palette) {
        guiGraphics.fill(n + 6, n2 + 7, n + n3 + 6, n2 + n4 + 7, palette.shadow());
        if (interfaceStyle != ClientConfig.InterfaceStyle.MINIMAL) {
            guiGraphics.fill(n - 2, n2 - 2, n + n3 + 2, n2 + n4 + 2, palette.outer());
            guiGraphics.fill(n - 1, n2 - 1, n + n3 + 1, n2 + n4 + 1, palette.border());
        }
        guiGraphics.fill(n, n2, n + n3, n2 + n4, palette.panel());
        guiGraphics.fill(n + 1, n2 + 1, n + n3 - 1, n2 + 32, palette.header());
        ThemeAmbientRenderer.drawPanel(guiGraphics, n, n2, n3, n4, interfaceStyle, palette);
        switch (interfaceStyle) {
            case STANDARD: {
                guiGraphics.fill(n, n2, n + n3, n2 + 2, palette.accentA());
                guiGraphics.fill(n, n2 + 32, n + n3, n2 + 33, palette.accentB());
                guiGraphics.fill(n + 2, n2 + 2, n + 3, n2 + n4 - 2, -12762290);
                break;
            }
            case FOREVER: {
                // Integrated megabase/network shell: the two primary theme colours
                // frame smaller subsystem accents rather than acting as the whole identity.
                guiGraphics.fill(n, n2, n + n3, n2 + 2, 0xFF55D6FF);
                guiGraphics.fill(n + 2, n2 + 2, n + 4, n2 + n4 - 2, 0xFF243747);
                guiGraphics.fill(n + n3 - 4, n2 + 2, n + n3 - 2, n2 + n4 - 2, 0xFF243747);
                guiGraphics.fill(n + 10, n2 + 28, n + n3 - 10, n2 + 30, 0xFF0C1620);

                int busY = n2 + 29;
                int[] systemColors = {0xFFC9D3DD, 0xFF55D6C7, 0xFFFF8A3D, 0xFFB66BFF};
                int usable = Math.max(40, n3 - 44);
                for (int i = 0; i < systemColors.length; ++i) {
                    int px = n + 20 + i * usable / 4;
                    guiGraphics.fill(px, busY - 3, px + 12, busY - 1, GuiMotion.alpha(systemColors[i], 108));
                    guiGraphics.fill(px + 5, busY - 1, px + 7, busY + 3, GuiMotion.alpha(systemColors[i], 72));
                }
                guiGraphics.fill(n + n3 - 29, n2 + 8, n + n3 - 14, n2 + 22, 0xFF101A24);
                guiGraphics.fill(n + n3 - 26, n2 + 11, n + n3 - 17, n2 + 19, GuiMotion.alpha(palette.accentA(), 92));
                guiGraphics.fill(n + n3 - 23, n2 + 13, n + n3 - 20, n2 + 17, GuiMotion.alpha(palette.accentB(), 112));
                break;
            }
            case AE2: {
                // ME Controller / Terminal shell: cool metal plates around a deep
                // graphite inset, with controller cells as the only colourful accents.
                guiGraphics.fill(n, n2, n + n3, n2 + 3, 0xFFE2E5E9);
                guiGraphics.fill(n + 1, n2 + 3, n + 3, n2 + n4 - 2, 0xFF6C737D);
                guiGraphics.fill(n + n3 - 3, n2 + 3, n + n3 - 1, n2 + n4 - 2, 0xFF4B515A);
                guiGraphics.fill(n + 8, n2 + 27, n + n3 - 8, n2 + 31, 0xFF17191E);
                guiGraphics.fill(n + 10, n2 + 28, n + n3 - 10, n2 + 29, 0xFF626973);

                for (int i = 0; i < 3; ++i) {
                    int cx = n + 14 + i * 15;
                    guiGraphics.fill(cx, n2 + 7, cx + 12, n2 + 19, 0xFF24272D);
                    guiGraphics.fill(cx + 2, n2 + 9, cx + 10, n2 + 17,
                            GuiMotion.alpha(i == 1 ? palette.accentB() : palette.accentA(), 78));
                }
                guiGraphics.fill(n + n3 - 43, n2 + 8, n + n3 - 14, n2 + 20, 0xFF17191E);
                for (int i = 0; i < 3; ++i) {
                    guiGraphics.fill(n + n3 - 39 + i * 8, n2 + 11, n + n3 - 34 + i * 8, n2 + 17, 0xFF3B4048);
                }
                break;
            }
            case ORITECH: {
                // Oritech uses a clean machine-frame silhouette instead of repeated
                // hazard dashes: dark graphite body, orange structural rail and cyan energy glass.
                guiGraphics.fill(n, n2, n + n3, n2 + 3, 0xFF11171A);
                guiGraphics.fill(n + 2, n2 + 3, n + 5, n2 + n4 - 3, palette.accentA());
                guiGraphics.fill(n + n3 - 5, n2 + 3, n + n3 - 3, n2 + n4 - 3, palette.border());
                guiGraphics.fill(n + 5, n2 + 3, n + n3 - 5, n2 + 5, palette.border());

                guiGraphics.fill(n + 16, n2 + 8, n + 42, n2 + 20, 0xFF111A1D);
                guiGraphics.fill(n + 18, n2 + 10, n + 40, n2 + 18, GuiMotion.alpha(palette.accentB(), 88));
                guiGraphics.fill(n + n3 - 42, n2 + 8, n + n3 - 16, n2 + 20, 0xFF111A1D);
                guiGraphics.fill(n + n3 - 40, n2 + 10, n + n3 - 18, n2 + 18, GuiMotion.alpha(palette.accentA(), 72));

                InterfaceTheme.rivet(guiGraphics, n + 8, n2 + 7, palette.accentA());
                InterfaceTheme.rivet(guiGraphics, n + n3 - 10, n2 + 7, palette.accentB());
                InterfaceTheme.rivet(guiGraphics, n + 8, n2 + n4 - 10, palette.border());
                InterfaceTheme.rivet(guiGraphics, n + n3 - 10, n2 + n4 - 10, palette.border());
                break;
            }
            case MEKANISM: {
                // Mekanism GUI vocabulary: clean machine casing, compact side ports,
                // one energy gauge and no repeating decorative rails.
                guiGraphics.fill(n, n2, n + n3, n2 + 3, 0xFF384B52);
                guiGraphics.fill(n + 2, n2 + 3, n + 4, n2 + n4 - 3, 0xFF62747A);
                guiGraphics.fill(n + n3 - 4, n2 + 3, n + n3 - 2, n2 + n4 - 3, 0xFF36484F);
                guiGraphics.fill(n + 10, n2 + 27, n + n3 - 10, n2 + 30, 0xFF10191D);

                // Input/output configuration tabs.
                guiGraphics.fill(n + 7, n2 + 9, n + 11, n2 + 15, 0xFF4E8BD8);
                guiGraphics.fill(n + 7, n2 + 17, n + 11, n2 + 23, 0xFFD85858);
                guiGraphics.fill(n + n3 - 12, n2 + 8, n + n3 - 6, n2 + 24, 0xFF132127);
                guiGraphics.fill(n + n3 - 10, n2 + 11, n + n3 - 8, n2 + 21, GuiMotion.alpha(palette.accentA(), 102));

                // Compact processing indicator in the title rail.
                guiGraphics.fill(n + n3 / 2 - 11, n2 + 10, n + n3 / 2 + 12, n2 + 20, 0xFF132127);
                guiGraphics.fill(n + n3 / 2 - 8, n2 + 13, n + n3 / 2 + 6, n2 + 17, GuiMotion.alpha(palette.accentB(), 74));
                guiGraphics.fill(n + n3 / 2 + 6, n2 + 14, n + n3 / 2 + 10, n2 + 16, GuiMotion.alpha(palette.accentB(), 74));
                break;
            }
            case QUANTUM: {
                // One coherent AdvancedAE-style compute assembly; every decorative
                // element belongs to the same casing instead of floating independently.
                guiGraphics.fill(n, n2, n + n3, n2 + 3, 0xFF777981);
                guiGraphics.fill(n + 2, n2 + 3, n + 4, n2 + n4 - 3, 0xFF484A52);
                guiGraphics.fill(n + n3 - 4, n2 + 3, n + n3 - 2, n2 + n4 - 3, 0xFF484A52);
                guiGraphics.fill(n + 10, n2 + 28, n + n3 - 10, n2 + 30, 0xFF17151C);

                int qx = n + n3 - 48;
                int qy = n2 + 7;
                guiGraphics.fill(qx, qy, qx + 34, qy + 18, 0xFF24212A);
                for (int i = 0; i < 3; ++i) {
                    int cellX = qx + 3 + i * 10;
                    guiGraphics.fill(cellX, qy + 3, cellX + 8, qy + 15, 0xFF3A3541);
                }
                guiGraphics.fill(qx + 13, qy + 5, qx + 21, qy + 13, 0xFF3A1B58);
                guiGraphics.fill(qx + 15, qy + 7, qx + 19, qy + 11, 0xFF8C43D6);
                guiGraphics.fill(qx + 4, qy + 16, qx + 30, qy + 18, 0xFFD2A62B);
                break;
            }
            case HOLOGRAPHIC: {
                guiGraphics.fill(n + 1, n2 + 1, n + n3 - 1, n2 + 2, palette.accentA());
                guiGraphics.fill(n + 1, n2 + n4 - 2, n + n3 - 1, n2 + n4 - 1, palette.accentA());
                for (int i = n2 + 36; i < n2 + n4 - 3; i += 7) {
                    guiGraphics.fill(n + 3, i, n + 4, i + 3, palette.accentB());
                }
                break;
            }
            case MONOCHROME: {
                guiGraphics.fill(n, n2, n + n3, n2 + 2, palette.accentA());
                guiGraphics.fill(n + 12, n2 + 32, n + n3 - 12, n2 + 33, palette.border());
                break;
            }
            case MINIMAL: {
                guiGraphics.fill(n + 14, n2 + 31, n + n3 - 14, n2 + 32, palette.accentA());
                break;
            }
            case CARBON, TERMINAL, DEEP_SPACE, COPPER, AURORA, REDSTONE, FROST, NATURE, CAT: {
                int split = n + n3 / 3;
                guiGraphics.fill(n, n2, split, n2 + 3, palette.accentA());
                guiGraphics.fill(split, n2, n + n3, n2 + 3, palette.accentB());
                switch (interfaceStyle) {
                    case TERMINAL -> {
                        for (int y = n2 + 36; y < n2 + n4 - 4; y += 9) {
                            guiGraphics.fill(n + 3, y, n + 5, y + 4, palette.accentA());
                        }
                    }
                    case DEEP_SPACE -> {
                        guiGraphics.fill(n + 8, n2 + n4 - 10, n + 10, n2 + n4 - 8, palette.accentA());
                        guiGraphics.fill(n + n3 / 2, n2 + n4 - 16, n + n3 / 2 + 1, n2 + n4 - 15, palette.text());
                        guiGraphics.fill(n + n3 - 14, n2 + n4 - 8, n + n3 - 12, n2 + n4 - 6, palette.accentB());
                    }
                    case COPPER -> {
                        InterfaceTheme.rivet(guiGraphics, n + 7, n2 + 7, palette.accentA());
                        InterfaceTheme.rivet(guiGraphics, n + n3 - 9, n2 + 7, palette.accentB());
                        guiGraphics.fill(n + 4, n2 + 34, n + 6, n2 + n4 - 5, palette.border());
                    }
                    case AURORA -> {
                        guiGraphics.fill(n + 4, n2 + n4 - 5, n + n3 / 2, n2 + n4 - 3, palette.accentA());
                        guiGraphics.fill(n + n3 / 2, n2 + n4 - 5, n + n3 - 4, n2 + n4 - 3, palette.accentB());
                    }
                    case REDSTONE -> {
                        InterfaceTheme.circuit(guiGraphics, n + 8, n2 + n4 - 6, palette.accentA(), true);
                        InterfaceTheme.circuit(guiGraphics, n + n3 - 8, n2 + n4 - 6, palette.accentB(), false);
                        guiGraphics.fill(n + n3 / 2 - 1, n2 + 33, n + n3 / 2 + 1, n2 + 39, palette.accentA());
                    }
                    case FROST -> {
                        InterfaceTheme.corner(guiGraphics, n + 5, n2 + 6, palette.accentB());
                        InterfaceTheme.corner(guiGraphics, n + n3 - 12, n2 + n4 - 10, palette.accentA());
                        guiGraphics.fill(n + n3 - 5, n2 + 5, n + n3 - 3, n2 + n4 / 2, palette.accentB());
                    }
                    case NATURE -> {
                        guiGraphics.fill(n + 3, n2 + 34, n + 5, n2 + n4 - 5, palette.accentA());
                        guiGraphics.fill(n + n3 - 5, n2 + 34, n + n3 - 4, n2 + Math.min(n4 - 5, 108), InterfaceTheme.darken(palette.accentA(), 0.72f));
                        guiGraphics.fill(n + 5, n2 + n4 - 12, n + 13, n2 + n4 - 10, palette.accentA());
                        guiGraphics.fill(n + 5, n2 + n4 - 25, n + 10, n2 + n4 - 23, palette.accentA());
                        int blossomX = n + n3 - 14;
                        int blossomY = n2 + 14;
                        guiGraphics.fill(blossomX - 1, blossomY - 4, blossomX + 2, blossomY - 1, palette.accentB());
                        guiGraphics.fill(blossomX - 1, blossomY + 1, blossomX + 2, blossomY + 4, palette.accentB());
                        guiGraphics.fill(blossomX - 4, blossomY - 1, blossomX - 1, blossomY + 2, palette.accentB());
                        guiGraphics.fill(blossomX + 1, blossomY - 1, blossomX + 4, blossomY + 2, palette.accentB());
                        guiGraphics.fill(blossomX, blossomY, blossomX + 1, blossomY + 1, palette.text());
                    }
                    case CAT -> {
                        CatThemeRenderer.drawHeader(guiGraphics, n, n2, n3, palette);
                    }
                    default -> {
                    }
                }
                break;
            }
            case CUSTOM: {
                int n8 = n + n3 * 2 / 3;
                guiGraphics.fill(n, n2, n8, n2 + 2, palette.accentA());
                guiGraphics.fill(n8, n2, n + n3, n2 + 2, palette.accentB());
                guiGraphics.fill(n + 1, n2 + 2, n + 2, n2 + n4, palette.accentA());
                guiGraphics.fill(n + n3 - 2, n2 + 2, n + n3 - 1, n2 + n4, palette.accentB());
            }
        }
    }

    public static void drawButtonDecoration(GuiGraphics guiGraphics, int x, int y, int width, int height, boolean active, float hoverProgress) {
        ClientConfig.InterfaceStyle style = (ClientConfig.InterfaceStyle)ClientConfig.VALUES.interfaceStyle.get();
        Palette palette = InterfaceTheme.current();
        int accent = active ? palette.accentB() : palette.accentA();
        ThemeAmbientRenderer.drawButton(guiGraphics, x, y, width, height, style, palette, active, hoverProgress);
        switch (style) {
            case CARBON -> {
                InterfaceTheme.rivet(guiGraphics, x + 4, y + height - 3, palette.border());
                InterfaceTheme.rivet(guiGraphics, x + width - 5, y + height - 3, palette.border());
            }
            case TERMINAL -> {
                guiGraphics.fill(x + 3, y + 3, x + 4, y + height - 3, accent);
                if (hoverProgress > 0.0f) {
                    int scanY = y + 2 + (int)((height - 4) * hoverProgress);
                    guiGraphics.fill(x + 5, scanY, x + width - 3, scanY + 1, GuiMotion.alpha(accent, 120));
                }
            }
            case DEEP_SPACE -> {
                guiGraphics.fill(x + 4, y + 3, x + 5, y + 4, accent);
                guiGraphics.fill(x + width - 7, y + height - 4, x + width - 5, y + height - 2, palette.accentB());
            }
            case COPPER -> {
                InterfaceTheme.rivet(guiGraphics, x + 3, y + 3, accent);
                InterfaceTheme.rivet(guiGraphics, x + width - 5, y + height - 5, palette.border());
            }
            case AURORA -> {
                int split = x + width / 2;
                guiGraphics.fill(x + 2, y + height - 2, split, y + height - 1, palette.accentA());
                guiGraphics.fill(split, y + height - 2, x + width - 2, y + height - 1, palette.accentB());
            }
            case REDSTONE -> {
                int center = x + width / 2;
                guiGraphics.fill(x + 3, y + height - 3, center, y + height - 2, accent);
                guiGraphics.fill(center, y + height - 5, center + 1, y + height - 2, accent);
                guiGraphics.fill(center - 1, y + height - 6, center + 2, y + height - 4, palette.accentB());
            }
            case FROST -> {
                InterfaceTheme.corner(guiGraphics, x + 2, y + 2, palette.accentB());
                guiGraphics.fill(x + width - 5, y + height - 3, x + width - 2, y + height - 2, palette.accentA());
            }
            case NATURE -> {
                guiGraphics.fill(x + 3, y + height - 3, x + width - 4, y + height - 2, palette.accentA());
                guiGraphics.fill(x + width / 3, y + height - 5, x + width / 3 + 2, y + height - 2, palette.accentB());
                guiGraphics.fill(x + width * 2 / 3, y + height - 6, x + width * 2 / 3 + 2, y + height - 2, palette.accentA());
            }
            case CAT -> {
                guiGraphics.fill(x + 3, y + height - 2, x + width - 3, y + height - 1,
                        GuiMotion.alpha(active ? palette.accentB() : accent, active ? 210 : 90));
            }
            default -> {
            }
        }
    }

    public static void drawTableDecoration(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        ClientConfig.InterfaceStyle style = (ClientConfig.InterfaceStyle)ClientConfig.VALUES.interfaceStyle.get();
        Palette palette = InterfaceTheme.current();
        ThemeAmbientRenderer.drawTable(guiGraphics, x, y, width, height, style, palette);
        switch (style) {
            case CARBON -> guiGraphics.fill(x + 3, y + 3, x + width - 3, y + 4, palette.border());
            case TERMINAL -> {
                for (int rowY = y + 3; rowY < y + height - 2; rowY += 8) {
                    guiGraphics.fill(x + 2, rowY, x + width - 2, rowY + 1, GuiMotion.alpha(palette.accentA(), 34));
                }
            }
            case DEEP_SPACE -> {
                guiGraphics.fill(x + width / 4, y + 5, x + width / 4 + 1, y + 6, palette.accentA());
                guiGraphics.fill(x + width * 3 / 4, y + height - 7, x + width * 3 / 4 + 2, y + height - 5, palette.accentB());
            }
            case COPPER -> {
                InterfaceTheme.rivet(guiGraphics, x + 3, y + 3, palette.accentA());
                InterfaceTheme.rivet(guiGraphics, x + width - 5, y + height - 5, palette.accentB());
            }
            case AURORA -> {
                guiGraphics.fill(x + 2, y + 2, x + width / 2, y + 3, palette.accentA());
                guiGraphics.fill(x + width / 2, y + 2, x + width - 2, y + 3, palette.accentB());
            }
            case REDSTONE -> InterfaceTheme.circuit(guiGraphics, x + 6, y + height - 4, palette.accentA(), true);
            case FROST -> InterfaceTheme.corner(guiGraphics, x + 3, y + 3, palette.accentB());
            case NATURE -> {
                guiGraphics.fill(x + 2, y + 3, x + 4, y + height - 3, palette.accentA());
                guiGraphics.fill(x + 4, y + height / 2, x + 8, y + height / 2 + 2, palette.accentB());
            }
            case CAT -> {
                guiGraphics.fill(x + 2, y + 2, x + 4, y + height - 2, GuiMotion.alpha(palette.accentA(), 95));
            }
            default -> {
            }
        }
    }

    private static void catEar(GuiGraphics g, int x, int y, int color, boolean left) {
        int dir = left ? 1 : -1;
        g.fill(Math.min(x, x + dir * 6), y + 5, Math.max(x, x + dir * 6) + 1, y + 7, GuiMotion.alpha(color, 92));
        g.fill(Math.min(x + dir, x + dir * 5), y + 3, Math.max(x + dir, x + dir * 5) + 1, y + 5, GuiMotion.alpha(color, 112));
        g.fill(Math.min(x + dir * 2, x + dir * 4), y + 1, Math.max(x + dir * 2, x + dir * 4) + 1, y + 3, GuiMotion.alpha(color, 128));
        g.fill(Math.min(x + dir * 3, x + dir * 4), y, Math.max(x + dir * 3, x + dir * 4) + 1, y + 2, GuiMotion.alpha(0xFFFFEAF5, 86));
    }

    private static void catPaw(GuiGraphics g, int x, int y, int color) {
        g.fill(x + 2, y + 3, x + 6, y + 6, color);
        g.fill(x, y + 1, x + 2, y + 3, color);
        g.fill(x + 2, y, x + 4, y + 2, color);
        g.fill(x + 5, y + 1, x + 7, y + 3, color);
    }

    private static void corner(GuiGraphics guiGraphics, int n, int n2, int n3) {
        guiGraphics.fill(n, n2, n + 7, n2 + 1, n3);
        guiGraphics.fill(n, n2, n + 1, n2 + 7, n3);
    }

    private static void circuit(GuiGraphics guiGraphics, int n, int n2, int n3, boolean bl) {
        int n4 = bl ? 1 : -1;
        int n5 = n + n4 * 13;
        guiGraphics.fill(Math.min(n, n5), n2, Math.max(n, n5), n2 + 1, n3);
        int n6 = n + n4 * 8;
        guiGraphics.fill(n6, n2 - 3, n6 + 1, n2 + 1, n3);
        int n7 = n + n4 * 12;
        guiGraphics.fill(n7 - 1, n2 - 1, n7 + 2, n2 + 2, n3);
    }

    private static void rivet(GuiGraphics guiGraphics, int n, int n2, int n3) {
        guiGraphics.fill(n, n2, n + 2, n2 + 2, n3);
        guiGraphics.fill(n + 1, n2 + 1, n + 2, n2 + 2, -1);
    }

    private static int darken(int n, float f) {
        int n2 = (int)((float)(n >> 16 & 0xFF) * f);
        int n3 = (int)((float)(n >> 8 & 0xFF) * f);
        int n4 = (int)((float)(n & 0xFF) * f);
        return n & 0xFF000000 | n2 << 16 | n3 << 8 | n4;
    }

    private static int hsv(float f, float f2, float f3) {
        float f4;
        float f5;
        float f6 = (f - (float)Math.floor(f)) * 6.0f;
        int n = (int)f6;
        float f7 = f6 - (float)n;
        float f8 = f3 * (1.0f - f2);
        float f9 = f3 * (1.0f - f2 * f7);
        float f10 = f3 * (1.0f - f2 * (1.0f - f7));
        float blue = switch (n % 6) {
            case 0 -> {
                f5 = f3;
                f4 = f10;
                yield f8;
            }
            case 1 -> {
                f5 = f9;
                f4 = f3;
                yield f8;
            }
            case 2 -> {
                f5 = f8;
                f4 = f3;
                yield f10;
            }
            case 3 -> {
                f5 = f8;
                f4 = f9;
                yield f3;
            }
            case 4 -> {
                f5 = f10;
                f4 = f8;
                yield f3;
            }
            default -> {
                f5 = f3;
                f4 = f8;
                yield f9;
            }
        };
        return (int)(f5 * 255.0f) << 16 | (int)(f4 * 255.0f) << 8 | (int)(blue * 255.0f);
    }

    private static int opacity(int n, double d) {
        int n2 = (int)Math.round((double)(n >>> 24 & 0xFF) * d);
        return Math.max(0, Math.min(255, n2)) << 24 | n & 0xFFFFFF;
    }

    public record Palette(int backdrop, int shadow, int outer, int panel, int header, int tableOuter, int tableHeader, int rowEven, int rowOdd, int hover, int summary, int text, int muted, int accentA, int accentB, int border) {
        public Palette withOpacity(double d) {
            return new Palette(InterfaceTheme.opacity(this.backdrop, d), InterfaceTheme.opacity(this.shadow, d), InterfaceTheme.opacity(this.outer, d), InterfaceTheme.opacity(this.panel, d), InterfaceTheme.opacity(this.header, d), InterfaceTheme.opacity(this.tableOuter, d), InterfaceTheme.opacity(this.tableHeader, d), InterfaceTheme.opacity(this.rowEven, d), InterfaceTheme.opacity(this.rowOdd, d), InterfaceTheme.opacity(this.hover, d), InterfaceTheme.opacity(this.summary, d), this.text, this.muted, this.accentA, this.accentB, this.border);
        }
    }
}
