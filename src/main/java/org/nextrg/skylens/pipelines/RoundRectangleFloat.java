package org.nextrg.skylens.pipelines;

import earth.terrarium.olympus.client.pipelines.RoundedRectangle;
import earth.terrarium.olympus.client.pipelines.uniforms.RoundedRectangleUniform;
import earth.terrarium.olympus.client.utils.GuiGraphicsHelper;
import net.minecraft.client.gui.DrawContext;
import org.nextrg.skylens.pipelines.pips.RoundRectangleFloatPIPRenderer;

public class RoundRectangleFloat {
    /**
     * Draws a 2D rectangle with rounded corners on UI using Olympus rounded rectangle, but with float variables.
     * @param drawContext Context used to draw the ui element
     * @param x X position of <b>left</b> corner of the rectangle
     * @param y Y position of <b>top</b> corner of the rectangle
     *
     * @see RoundedRectangle RoundedRectangle</code> (original)
     * @see RoundedRectangleUniform
     * @see RoundRectangleFloatPIPRenderer
     */
    public static void draw(DrawContext drawContext, float x, float y, float width, float height, int backgroundColor, int borderColor, float borderRadius, float borderWidth) {
        RoundRectangleFloatPIPRenderer.State state = new RoundRectangleFloatPIPRenderer.State(
                drawContext,
                x, y,
                width, height,
                backgroundColor, borderColor,
                borderRadius, borderWidth
        );
        
        GuiGraphicsHelper.submitPip(drawContext, state);
    }
}