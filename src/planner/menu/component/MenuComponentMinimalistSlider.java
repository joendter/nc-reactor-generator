package planner.menu.component;
import org.lwjgl.glfw.GLFW;
import planner.Core;
import simplelibrary.image.Color;
import simplelibrary.opengl.gui.components.MenuComponent;
public class MenuComponentMinimalistSlider extends MenuComponent{
    public boolean enabled;
    public double textInset = -1;
    public double minimum;
    public double maximum;
    public double value;
    public final int digits;
    public boolean isPressed;
    public double sliderHeight;
    public double maxSliderX;
    public double sliderX;    
    private final boolean darker;
    private String name;
    public MenuComponentMinimalistSlider(double x, double y, double width, double height, String name, int minimum, int maximum, int initial, boolean enabled){
        this(x, y, width, height, name, minimum, maximum, initial, enabled, false);
    }
    public MenuComponentMinimalistSlider(double x, double y, double width, double height, String name, double minimum, double maximum, double initial, int digits, boolean enabled){
        this(x, y, width, height, name, minimum, maximum, initial, digits, enabled, false);
    }
    public MenuComponentMinimalistSlider(double x, double y, double width, double height, String name, int minimum, int maximum, int initial, boolean enabled, boolean darker){
        super(x, y, width, height);
        this.darker = darker;
        this.name = name;
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = initial;
        digits = 0;
        this.enabled = enabled;
        updateSlider();
    }
    public MenuComponentMinimalistSlider(double x, double y, double width, double height, String name, double minimum, double maximum, double initial, int digits, boolean enabled, boolean darker){
        super(x, y, width, height);
        this.darker = darker;
        this.name = name;
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = initial;
        this.digits = digits;
        this.enabled = enabled;
        updateSlider();
    }
    @Override
    public void onMouseButton(double x, double y, int button, boolean pressed, int mods) {
        if(button==GLFW.GLFW_MOUSE_BUTTON_LEFT&&pressed&&enabled){
            isPressed = true;
            updateSlider(x);
        }else if(button==GLFW.GLFW_MOUSE_BUTTON_LEFT&&!pressed){
            isPressed = false;
        }
    }
    @Override
    public void render(){
        updateSlider();
        sliderHeight = height/2;
        if(textInset<0){
            textInset = height/10;
        }
        Color col;
        if(darker){
             col = Core.theme.getBrighterDarkButtonColor();
            if(enabled){
                if(isPressed)col = Core.theme.getBrighterDarkerDarkerDarkButtonColor();
                else if(isMouseOver)col = Core.theme.getBrighterBrighterDarkButtonColor();
            }else{
                col = Core.theme.getBrighterDarkerDarkerDarkButtonColor();
            }
        }else{
            col = Core.theme.getBrighterButtonColor();
            if(enabled){
                if(isPressed)col = Core.theme.getBrighterDarkerDarkerButtonColor();
                else if(isMouseOver)col = Core.theme.getBrighterBrighterButtonColor();
            }else{
                col = Core.theme.getBrighterDarkerButtonColor();
            }
        }
        Core.applyColor(darker?Core.theme.getDarkButtonColor():Core.theme.getButtonColor());
        drawRect(x, y, x+width, y+height, 0);
        Core.applyColor(col);
        drawRect(x+sliderX, y, x+sliderX+sliderHeight, y+sliderHeight, 0);
        Core.applyColor(Core.theme.getTextColor());
        drawCenteredText(x+textInset, y+sliderHeight+textInset, x+width-textInset, y+height-textInset, name+": "+getValueS());
    }
    @Override
    public void onMouseMove(double x, double y){
        if(isPressed)updateSlider(x);
    }
    @Override
    public void onMouseMovedElsewhere(double x, double y){
        if(isPressed)updateSlider(x);
    }
    private void updateSlider(double x){
        x-=sliderHeight/2;
        double percent = x/maxSliderX;
        if(percent>1){
            percent = 1;
        }else if(percent<0){
            percent = 0;
        }
        value = percent*(maximum-minimum)+minimum;
        updateSlider();
    }
    private void updateSlider(){
        sliderHeight = height/2;
        maxSliderX = width-sliderHeight;
        sliderX = 0;
        double percent = (getValue()-minimum)/(maximum-minimum);
        sliderX = maxSliderX*percent;
    }
    public String getValueS(){
        if(Math.round(getValue())==getValue()){
            return ""+Math.round(getValue());
        }else{
            return ""+getValue();
        }
    }
    public double getValue(){
        if(digits==0){
            return Math.round(value);
        }else{
            return (double)Math.round(value*digits)/digits;
        }
    }
    public void setValue(double value){
        this.value = Math.min(maximum, Math.max(minimum, value));
        updateSlider();
    }
    @Override
    public MenuComponentMinimalistSlider setTooltip(String tooltip){
        this.tooltip = tooltip;
        return this;
    }
}