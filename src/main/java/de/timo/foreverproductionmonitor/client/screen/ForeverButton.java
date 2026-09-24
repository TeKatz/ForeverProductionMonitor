/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Button$OnPress
 *  net.minecraft.network.chat.Component
 */
package de.timo.foreverproductionmonitor.client.screen;

import de.timo.foreverproductionmonitor.client.screen.InterfaceTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class ForeverButton
extends Button {
    private Style style;

    private ForeverButton(int n, int n2, int n3, int n4, Component component, Button.OnPress onPress, Style style) {
        super(n, n2, n3, n4, component, onPress, DEFAULT_NARRATION);
        this.style = style;
    }

    public static ForeverButton create(Component component, Button.OnPress onPress, Style style, int n, int n2, int n3, int n4) {
        return new ForeverButton(n, n2, n3, n4, component, onPress, style);
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    protected void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3;
        int n4;
        boolean bl = this.isHoveredOrFocused();
        if (!this.active) {
            n4 = -12960184;
        } else {
            switch (this.style) {
                default: {
                    throw new IncompatibleClassChangeError();
                }
                case GOLD: {
                    if (bl) {
                        n4 = -11654;
                        break;
                    }
                    n4 = -3041982;
                    break;
                }
                case VIOLET: {
                    if (bl) {
                        n4 = -3298561;
                        break;
                    }
                    n4 = -7575364;
                    break;
                }
                case SECONDARY: {
                    if (bl) {
                        n4 = -6576714;
                        break;
                    }
                    n4 = -10919568;
                    break;
                }
                case THEMED: {
                    if (bl) {
                        n4 = InterfaceTheme.current().accentA();
                        break;
                    }
                    n4 = InterfaceTheme.current().border();
                    break;
                }
                case THEMED_ACTIVE: {
                    n4 = bl ? InterfaceTheme.current().accentB() : InterfaceTheme.current().accentA();
                }
            }
        }
        if (!this.active) {
            n3 = -14407631;
        } else {
            switch (this.style) {
                default: {
                    throw new IncompatibleClassChangeError();
                }
                case GOLD: {
                    if (bl) {
                        n3 = -9022682;
                        break;
                    }
                    n3 = -11519715;
                    break;
                }
                case VIOLET: {
                    if (bl) {
                        n3 = -10729862;
                        break;
                    }
                    n3 = -12570031;
                    break;
                }
                case SECONDARY: {
                    if (bl) {
                        n3 = -12498859;
                        break;
                    }
                    n3 = -13617600;
                    break;
                }
                case THEMED: {
                    if (bl) {
                        n3 = InterfaceTheme.current().tableHeader();
                        break;
                    }
                    n3 = InterfaceTheme.current().summary();
                    break;
                }
                case THEMED_ACTIVE: {
                    n3 = bl ? InterfaceTheme.current().header() : InterfaceTheme.current().tableHeader();
                }
            }
        }
        int n5 = n3;
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), n4);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, n5);
        int n6 = this.active ? (this.style == Style.THEMED || this.style == Style.THEMED_ACTIVE ? InterfaceTheme.current().text() : -724502) : -8946554;
        this.renderScrollingString(guiGraphics, Minecraft.getInstance().font, 3, n6);
    }

    public static enum Style {
        GOLD,
        VIOLET,
        SECONDARY,
        THEMED,
        THEMED_ACTIVE;

    }
}

