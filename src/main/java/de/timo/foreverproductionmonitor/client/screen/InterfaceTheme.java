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
        return InterfaceTheme.palette((ClientConfig.InterfaceStyle)((Object)ClientConfig.VALUES.interfaceStyle.get()), (Integer)ClientConfig.VALUES.interfaceCustomHue.get(), (Integer)ClientConfig.VALUES.interfaceCustomSecondaryHue.get(), (Double)ClientConfig.VALUES.interfaceCustomBrightness.get()).withOpacity((Double)ClientConfig.VALUES.interfaceOpacity.get());
    }

    public static Palette palette(ClientConfig.InterfaceStyle interfaceStyle, int n) {
        return InterfaceTheme.palette(interfaceStyle, n, (n + 38) % 360, 0.52);
    }

    public static Palette palette(ClientConfig.InterfaceStyle interfaceStyle, int n, int n2, double d) {
        return switch (interfaceStyle) {
            default -> throw new IncompatibleClassChangeError();
            case ClientConfig.InterfaceStyle.STANDARD -> new Palette(1510542095, -2063597568, -16118768, -233037784, -14078408, -15723751, -13617599, -14670804, -15065563, 1716344420, -14209995, -986379, -7103576, -8484972, -4406324, -9932418);
            case ClientConfig.InterfaceStyle.FOREVER -> new Palette(1695024397, -2063597568, -16054256, -233105373, -14015949, -15725290, -13423810, -14607318, -15001822, 1717452408, -14015436, -724745, -6647388, -2516414, -6591279, -10926747);
            case ClientConfig.InterfaceStyle.AE2 -> new Palette(1610877970, -2063597568, -16379879, -233826272, -15257539, -16248037, -14928575, -15456721, -15720665, 1715515339, -15126212, -1378053, -7820876, -11941400, -6263328, -13214613);
            case ClientConfig.InterfaceStyle.ORITECH -> new Palette(1611466756, -2063597568, -15594745, -232974831, -13097705, -15594231, -12440805, -14148846, -14608881, 1718566173, -13294824, -3875, -5005171, -1606867, -1920177, -9354461);
            case ClientConfig.InterfaceStyle.MEKANISM -> new Palette(1610943760, -2063597568, -16314348, -233759712, -15125702, -16313833, -14597046, -15455697, -15719639, 1716709329, -15059395, -983041, -7227465, -14295865, -9633934, -13276048);
            case ClientConfig.InterfaceStyle.QUANTUM -> new Palette(1694893072, -2063597568, -16251631, -233237975, -13623228, -15923177, -12573350, -14412240, -14937816, 1719616216, -13492410, -462593, -5794122, -5026561, -19922, -9944441);
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
            case ClientConfig.InterfaceStyle.NATURE -> new Palette(0x72100904, -2063597568, 0xFF172514, 0xF121321C, 0xFF30452A, 0xFF111C0F, 0xFF3B5533, 0xFF263821, 0xFF1D2D1A, 0x6689C96D, 0xFF2D4327, 0xFFF0F6DD, 0xFFA6B18C, 0xFF79B85C, 0xFFD4A84E, 0xFF557348);
            case ClientConfig.InterfaceStyle.CUSTOM -> InterfaceTheme.customPalette(n, n2, d);
        };
    }

    private static Palette customPalette(int n, int n2, double d) {
        float f = (float)Math.floorMod(n, 360) / 360.0f;
        float f2 = (float)Math.floorMod(n2, 360) / 360.0f;
        float f3 = (float)Math.max(0.2, Math.min(0.9, d));
        int n3 = 0xFF000000 | InterfaceTheme.hsv(f, 0.62f, Math.min(1.0f, f3 + 0.4f));
        int n4 = 0xFF000000 | InterfaceTheme.hsv(f2, 0.52f, Math.min(1.0f, f3 + 0.48f));
        int n5 = 0xFF000000 | InterfaceTheme.hsv(f, 0.37f, 0.46f);
        int n6 = 0xFF000000 | InterfaceTheme.hsv(f, 0.25f, f3 * 0.43f);
        int n7 = 0xFF000000 | InterfaceTheme.hsv(f, 0.15f, f3 * 0.29f);
        return new Palette(1510344715, -2063597568, -16250354, -233301214, n6, -15855337, InterfaceTheme.darken(n6, 0.82f), n7, InterfaceTheme.darken(n7, 0.82f), 0x66000000 | n3 & 0xFFFFFF, InterfaceTheme.darken(n6, 0.78f), -855819, -6711904, n3, n4, n5);
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
                int n5 = n + n3 * 3 / 5;
                guiGraphics.fill(n, n2, n5, n2 + 2, palette.accentA());
                guiGraphics.fill(n5, n2, n + n3, n2 + 2, palette.accentB());
                guiGraphics.fill(n + 1, n2 + 32, n5, n2 + 33, InterfaceTheme.darken(palette.accentA(), 0.72f));
                guiGraphics.fill(n5, n2 + 32, n + n3 - 1, n2 + 33, InterfaceTheme.darken(palette.accentB(), 0.72f));
                InterfaceTheme.corner(guiGraphics, n + 4, n2 + 5, palette.accentA());
                InterfaceTheme.corner(guiGraphics, n + n3 - 8, n2 + n4 - 9, palette.accentB());
                break;
            }
            case AE2: {
                int n6 = n + n3 / 2;
                guiGraphics.fill(n, n2, n6, n2 + 2, palette.accentA());
                guiGraphics.fill(n6, n2, n + n3, n2 + 2, palette.accentB());
                guiGraphics.fill(n + 1, n2 + 2, n + 2, n2 + n4, palette.accentA());
                guiGraphics.fill(n + n3 - 2, n2 + 2, n + n3 - 1, n2 + n4, palette.accentB());
                InterfaceTheme.circuit(guiGraphics, n + 7, n2 + n4 - 5, palette.accentA(), true);
                InterfaceTheme.circuit(guiGraphics, n + n3 - 7, n2 + n4 - 5, palette.accentB(), false);
                break;
            }
            case ORITECH: {
                guiGraphics.fill(n, n2, n + n3, n2 + 3, -14346740);
                for (int i = 0; i < n3; i += 18) {
                    guiGraphics.fill(n + i, n2, n + Math.min(n3, i + 12), n2 + 2, i / 18 % 2 == 0 ? palette.accentA() : palette.accentB());
                }
                guiGraphics.fill(n + 2, n2 + 3, n + 4, n2 + n4 - 3, palette.accentA());
                InterfaceTheme.rivet(guiGraphics, n + 7, n2 + 7, palette.accentB());
                InterfaceTheme.rivet(guiGraphics, n + n3 - 9, n2 + 7, palette.accentB());
                InterfaceTheme.rivet(guiGraphics, n + 7, n2 + n4 - 9, palette.accentA());
                InterfaceTheme.rivet(guiGraphics, n + n3 - 9, n2 + n4 - 9, palette.accentA());
                break;
            }
            case MEKANISM: {
                guiGraphics.fill(n, n2, n + n3, n2 + 2, palette.accentA());
                guiGraphics.fill(n + 4, n2 + 5, n + 6, n2 + n4 - 5, palette.accentB());
                guiGraphics.fill(n + n3 - 6, n2 + 5, n + n3 - 4, n2 + n4 - 5, palette.accentA());
                InterfaceTheme.circuit(guiGraphics, n + 10, n2 + n4 - 6, palette.accentA(), true);
                InterfaceTheme.circuit(guiGraphics, n + n3 - 10, n2 + n4 - 6, palette.accentB(), false);
                break;
            }
            case QUANTUM: {
                int n7 = n + n3 / 2;
                guiGraphics.fill(n, n2, n7, n2 + 2, palette.accentA());
                guiGraphics.fill(n7, n2, n + n3, n2 + 2, palette.accentB());
                InterfaceTheme.corner(guiGraphics, n + 4, n2 + 5, palette.accentA());
                InterfaceTheme.corner(guiGraphics, n + n3 - 11, n2 + 5, palette.accentB());
                guiGraphics.fill(n7 - 2, n2 + n4 - 2, n7 + 2, n2 + n4, palette.accentB());
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
            case CARBON, TERMINAL, DEEP_SPACE, COPPER, AURORA, REDSTONE, FROST, NATURE: {
                int split = n + n3 / 3;
                guiGraphics.fill(n, n2, split, n2 + 3, palette.accentA());
                guiGraphics.fill(split, n2, n + n3, n2 + 3, palette.accentB());
                switch (interfaceStyle) {
                    case CARBON -> {
                        for (int x = n + 5; x < n + n3 - 8; x += 22) {
                            guiGraphics.fill(x, n2 + n4 - 4, x + 13, n2 + n4 - 2, palette.border());
                        }
                    }
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
                        guiGraphics.fill(n + 5, n2 + n4 - 12, n + 11, n2 + n4 - 10, palette.accentB());
                        guiGraphics.fill(n + 5, n2 + n4 - 25, n + 9, n2 + n4 - 23, palette.accentA());
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
                for (int stripeX = x + 3; stripeX < x + width - 3; stripeX += 8) {
                    guiGraphics.fill(stripeX, y + height - 2, Math.min(stripeX + 4, x + width - 2), y + height - 1, palette.border());
                }
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
            default -> {
            }
        }
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
