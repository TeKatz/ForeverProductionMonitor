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
            case CUSTOM: {
                int n8 = n + n3 * 2 / 3;
                guiGraphics.fill(n, n2, n8, n2 + 2, palette.accentA());
                guiGraphics.fill(n8, n2, n + n3, n2 + 2, palette.accentB());
                guiGraphics.fill(n + 1, n2 + 2, n + 2, n2 + n4, palette.accentA());
                guiGraphics.fill(n + n3 - 2, n2 + 2, n + n3 - 1, n2 + n4, palette.accentB());
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
