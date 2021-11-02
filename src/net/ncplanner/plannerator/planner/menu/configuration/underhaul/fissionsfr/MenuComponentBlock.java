package net.ncplanner.plannerator.planner.menu.configuration.underhaul.fissionsfr;
import net.ncplanner.plannerator.Renderer;
import net.ncplanner.plannerator.multiblock.configuration.underhaul.fissionsfr.Block;
import net.ncplanner.plannerator.planner.Core;
import net.ncplanner.plannerator.planner.menu.component.MenuComponentMinimalistButton;
import org.lwjgl.opengl.GL11;
import simplelibrary.opengl.gui.components.MenuComponent;
public class MenuComponentBlock extends MenuComponent{
    public final Block block;
    public final MenuComponentMinimalistButton edit = add(new MenuComponentMinimalistButton(0, 0, 0, 0, "", true, true, true){
        @Override
        public void renderForeground(){
            Renderer renderer = new Renderer();
            super.renderForeground();
            renderer.setColor(Core.theme.getComponentTextColor(Core.getThemeIndex(this)));
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glVertex2d(x+width*.25, y+height*.75);
            GL11.glVertex2d(x+width*.375, y+height*.75);
            GL11.glVertex2d(x+width*.25, y+height*.625);
            GL11.glEnd();
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2d(x+width*.4, y+height*.725);
            GL11.glVertex2d(x+width*.275, y+height*.6);
            GL11.glVertex2d(x+width*.5, y+height*.375);
            GL11.glVertex2d(x+width*.625, y+height*.5);

            GL11.glVertex2d(x+width*.525, y+height*.35);
            GL11.glVertex2d(x+width*.65, y+height*.475);
            GL11.glVertex2d(x+width*.75, y+height*.375);
            GL11.glVertex2d(x+width*.625, y+height*.25);
            GL11.glEnd();
        }
        @Override
        public void action(){
            onEditPressed.run();
        }
    }.setTooltip("Modify block"));
    public final MenuComponentMinimalistButton delete = add(new MenuComponentMinimalistButton(0, 0, 0, 0, "", true, true, true){
        @Override
        public void renderForeground(){
            Renderer renderer = new Renderer();
            super.renderForeground();
            renderer.setColor(Core.theme.getComponentTextColor(Core.getThemeIndex(this)));
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2d(x+width*.1, y+height*.8);
            GL11.glVertex2d(x+width*.2, y+height*.9);
            GL11.glVertex2d(x+width*.9, y+height*.2);
            GL11.glVertex2d(x+width*.8, y+height*.1);
            
            GL11.glVertex2d(x+width*.1, y+height*.2);
            GL11.glVertex2d(x+width*.2, y+height*.1);
            GL11.glVertex2d(x+width*.9, y+height*.8);
            GL11.glVertex2d(x+width*.8, y+height*.9);
            GL11.glEnd();
        }
        @Override
        public void action(){
            onDeletePressed.run();
        }
    }.setTooltip("Delete block"));
    private final Runnable onEditPressed;
    private final Runnable onDeletePressed;
    public MenuComponentBlock(Block block, Runnable onEditPressed, Runnable onDeletePressed){
        super(0, 0, 0, 100);
        this.block = block;
        this.onEditPressed = onEditPressed;
        this.onDeletePressed = onDeletePressed;
    }
    @Override
    public void renderBackground(){
        super.renderBackground();
        delete.x = width-height/2-height/4;
        edit.x = delete.x - height;
        delete.y = edit.y = height/4;
        delete.width = delete.height = edit.width = edit.height = height/2;
    }
    @Override
    public void render(){
        Renderer renderer = new Renderer();
        if(isMouseOver)renderer.setColor(Core.theme.getMouseoverUnselectableComponentColor(Core.getThemeIndex(this)));
        else renderer.setColor(Core.theme.getComponentColor(Core.getThemeIndex(this)));
        drawRect(x, y, x+width, y+height, 0);
    }
    @Override
    public void renderForeground(){
        Renderer renderer = new Renderer();
        renderer.setWhite();
        if(block.texture!=null)drawRect(x, y, x+height, y+height, Core.getTexture(block.displayTexture));
        renderer.setColor(Core.theme.getComponentTextColor(Core.getThemeIndex(this)));
        drawText(x+height, y, x+width, y+height/5, block.getDisplayName());
        if(block.cooling!=0)drawText(x+height, y+height/5, x+width, y+height/5*2, "Cooling: "+block.cooling+" H/t");
        if(block.fuelCell)drawText(x+height, y+height/5*2, x+width, y+height/5*3, "Fuel Cell");
        if(block.moderator)drawText(x+height, y+height/5*3, x+width, y+height/5*4, "Moderator");
        if(block.active!=null)drawText(x+height, y+height/5*4, x+width, y+height/5*5, "Active: "+block.active);
    }
}