package net.ncplanner.plannerator.planner.menu.component;
import net.ncplanner.plannerator.Renderer;
import net.ncplanner.plannerator.planner.Core;
import net.ncplanner.plannerator.planner.module.Module;
import org.lwjgl.glfw.GLFW;
import simplelibrary.font.FontManager;
import simplelibrary.opengl.gui.components.MenuComponent;
public class MenuComponentModule extends MenuComponent{
    public final Module module;
    public boolean enabled = false;
    public int min = 0;
    public int max = 0;
    public MenuComponentModule(Module module){
        super(0, 0, 0, 64);
        this.module = module;
        enabled = module.isActive();
    }
    @Override
    public void render(){
        Renderer renderer = new Renderer();
        if(isSelected){
            if(isMouseOver)renderer.setColor(Core.theme.getMouseoverSelectedComponentColor(Core.getThemeIndex(this)));
            else renderer.setColor(Core.theme.getSelectedComponentColor(Core.getThemeIndex(this)));
        }else{
            if(isMouseOver)renderer.setColor(Core.theme.getMouseoverComponentColor(Core.getThemeIndex(this)));
            else renderer.setColor(Core.theme.getComponentColor(Core.getThemeIndex(this)));
        }
        drawRect(x, y, x+width, y+height, 0);
        renderer.setColor(Core.theme.getComponentTextColor(Core.getThemeIndex(this)));
        drawText(module.getDisplayName()+" ("+(module.isActive()?"Active":"Inactive")+")");
    }
    public void drawText(String text){
        if(Core.isControlPressed()){
            if(max==0){
                text = min+"+";
            }else if(min==max){
                text = min+"";
            }else{
                text = min+"-"+max;
            }
        }
        double textLength = FontManager.getLengthForStringWithHeight(text, height);
        double scale = Math.min(1, width/textLength);
        double textHeight = (int)(height*scale)-1;
        drawText(x, y+height/2-textHeight/2, x+width, y+height/2+textHeight/2, text);
    }
    @Override
    public String getTooltip(){
        return module.getDescription();
    }
    @Override
    public boolean onMouseScrolled(double x, double y, double dx, double dy){
        if(super.onMouseScrolled(x, y, dx, dy))return true;
        if(isMouseOver&&Core.isControlPressed()){
            if(Core.isShiftPressed()){
                min+=dy;
            }else{
                max+=dy;
            }
            if(min<0)min = 0;
            if(max<0)max = 0;
            if(min>max)min = max;
            return true;
        }
        return false;
    }
    @Override
    public void onMouseButton(double x, double y, int button, boolean pressed, int mods){
        super.onMouseButton(x, y, button, pressed, mods);
        if(button==GLFW.GLFW_MOUSE_BUTTON_LEFT&&pressed){
            enabled = !enabled;
            module.setActive(enabled);
        }
    }
}