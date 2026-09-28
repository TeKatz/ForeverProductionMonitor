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

import de.timo.foreverproductionmonitor.client.ClientConfig;
import de.timo.foreverproductionmonitor.client.screen.InterfaceTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class ForeverButton
extends Button {
    private Style style;
    private Role role = Role.BUTTON;
    private float hoverProgress;
    private long lastRenderNanos;
    private long styleChangedNanos;

    private ForeverButton(int n, int n2, int n3, int n4, Component component, Button.OnPress onPress, Style style) {
        super(n, n2, n3, n4, component, onPress, DEFAULT_NARRATION);
        this.style = style;
        this.styleChangedNanos = GuiMotion.now();
    }

    public static ForeverButton create(Component component, Button.OnPress onPress, Style style, int n, int n2, int n3, int n4) {
        return new ForeverButton(n, n2, n3, n4, component, onPress, style);
    }

    public void setStyle(Style style) {
        if (this.style != style) {
            this.styleChangedNanos = GuiMotion.now();
        }
        this.style = style;
    }

    public ForeverButton setRole(Role role) {
        this.role = role == null ? Role.BUTTON : role;
        return this;
    }

    protected void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3;
        int n4;
        boolean hovered = this.isHoveredOrFocused();
        boolean animations = GuiMotion.enabled();
        if (animations) {
            long now = GuiMotion.now();
            float deltaSeconds = this.lastRenderNanos == 0L ? 0.0f : Math.min(0.05f, (float)((double)(now - this.lastRenderNanos) / 1.0E9));
            this.lastRenderNanos = now;
            float step = deltaSeconds / 0.14f;
            this.hoverProgress = Math.max(0.0f, Math.min(1.0f, this.hoverProgress + (hovered ? step : -step)));
        } else {
            this.hoverProgress = hovered ? 1.0f : 0.0f;
        }
        boolean bl = animations ? false : hovered;

        boolean customThemedControl =
                (ClientConfig.InterfaceStyle)ClientConfig.VALUES.interfaceStyle.get()
                        == ClientConfig.InterfaceStyle.CUSTOM
                && (this.style == Style.THEMED
                    || this.style == Style.THEMED_ACTIVE
                    || this.style == Style.SECONDARY);
        if (customThemedControl) {
            InterfaceTheme.Palette palette = InterfaceTheme.current();
            CustomThemeRenderer.drawControl(guiGraphics, this.role == Role.TAB,
                    this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                    this.active, this.style == Style.THEMED_ACTIVE, this.hoverProgress, palette);
            int textColor = this.active ? palette.text() : palette.muted();
            this.renderScrollingString(guiGraphics, Minecraft.getInstance().font, 3, textColor);
            return;
        }

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
        if (animations && this.active && this.hoverProgress > 0.0f) {
            int alpha = (int)(this.hoverProgress * 72.0f);
            int accent = this.style == Style.THEMED_ACTIVE ? InterfaceTheme.current().accentB() : InterfaceTheme.current().accentA();
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), alpha << 24 | accent & 0xFFFFFF);
            guiGraphics.renderOutline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), accent);
        }
        InterfaceTheme.drawButtonDecoration(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.style == Style.THEMED_ACTIVE, this.hoverProgress);
        if (animations && this.active && this.style == Style.THEMED_ACTIVE) {
            float progress = GuiMotion.progress(this.styleChangedNanos, 180L);
            if (progress < 1.0f) {
                int highlightWidth = Math.max(2, (int)((float)this.getWidth() * GuiMotion.easeOut(progress)));
                guiGraphics.fill(this.getX(), this.getY() + this.getHeight() - 2, this.getX() + highlightWidth, this.getY() + this.getHeight(), InterfaceTheme.current().accentB());
            }
        }
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

    public static enum Role {
        BUTTON,
        TAB;
    }
}
