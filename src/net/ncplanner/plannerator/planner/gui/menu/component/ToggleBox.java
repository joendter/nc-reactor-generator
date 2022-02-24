package net.ncplanner.plannerator.planner.gui.menu.component;
import net.ncplanner.plannerator.graphics.Renderer;
import net.ncplanner.plannerator.planner.Core;
import net.ncplanner.plannerator.planner.gui.Component;
import static org.lwjgl.glfw.GLFW.*;
public class ToggleBox extends Component{
    public String text;
    private final boolean darker;
    private float textInset = 4;
    public boolean isToggledOn = false;//because isSelected is taken
    private float boxInset = 0.15f;
    public boolean enabled = true;
    public ToggleBox(float x, float y, float width, float height, String label){
        this(x, y, width, height, label, false);
    }
    public ToggleBox(float x, float y, float width, float height, String label, boolean isToggledOn){
        this(x, y, width, height, label, isToggledOn, false);
    }
    public ToggleBox(float x, float y, float width, float height, String label, boolean isToggledOn, boolean darker){
        super(x, y, width, height);
        this.isToggledOn = isToggledOn;
        this.text = label;
        this.darker = darker;
    }
    @Override
    public void draw(double deltaTime){
        Renderer renderer = new Renderer();
        renderer.setColor(darker?Core.theme.getSecondaryComponentColor(Core.getThemeIndex(this)):Core.theme.getComponentColor(Core.getThemeIndex(this)));
        renderer.fillRect(x, y, x+width, y+height);//why is this here?
        renderer.setColor(darker?Core.theme.getSecondaryToggleBoxBorderColor(Core.getThemeIndex(this)):Core.theme.getToggleBoxBorderColor(Core.getThemeIndex(this)));
        renderer.fillRect(x, y, x+height, y+height);
        renderer.setColor(isToggledOn?Core.theme.getToggleBoxMouseoverColor(Core.getThemeIndex(this)):Core.theme.getToggleBoxBackgroundColor(Core.getThemeIndex(this)));
        renderer.fillRect(x+boxInset*height, y+boxInset*height, x+height-boxInset*height, y+height-boxInset*height);
        if(isMouseFocused&&!enabled){
            renderer.setColor(Core.theme.getToggleBoxSelectedColor(Core.getThemeIndex(this)), .25f);
            renderer.fillRect(x+boxInset*height, y+boxInset*height, x+height-boxInset*height, y+height-boxInset*height);
        }
        renderer.setColor(Core.theme.getComponentTextColor(Core.getThemeIndex(this)));
        drawText(renderer);
    }
    public void drawText(Renderer renderer){
        float textLength = renderer.getStringWidth(text, height);
        float scale = Math.min(1, (width-height-textInset*2)/textLength);
        float textHeight = (int)((height-textInset*2)*scale)-4;
        renderer.drawText(x+height+textInset, y+height/2-textHeight/2, x+width-textInset, y+height/2+textHeight/2, text);
    }
    @Override
    public ToggleBox setTooltip(String tooltip){
        this.tooltip = tooltip;
        return this;
    }
    @Override
    public void onMouseButton(double x, double y, int button, int action, int mods){
        super.onMouseButton(x, y, button, action, mods);
        if(button==GLFW_MOUSE_BUTTON_LEFT&&action==GLFW_PRESS&&enabled)isToggledOn = !isToggledOn;
    }
}