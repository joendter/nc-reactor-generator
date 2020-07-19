package planner.menu.configuration.overhaul.fissionsfr;
import org.lwjgl.opengl.Display;
import planner.Core;
import multiblock.configuration.overhaul.fissionsfr.CoolantRecipe;
import planner.menu.component.MenuComponentMinimalistButton;
import planner.menu.component.MenuComponentMinimalistTextBox;
import simplelibrary.opengl.gui.GUI;
import simplelibrary.opengl.gui.Menu;
public class MenuCoolantRecipeConfiguration extends Menu{
    private final MenuComponentMinimalistTextBox name = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "Name", true));
    private final MenuComponentMinimalistTextBox input = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "Input", true));
    private final MenuComponentMinimalistTextBox output = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "Output", true));
    private final MenuComponentMinimalistTextBox heat = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "", true).setIntFilter());
    private final MenuComponentMinimalistTextBox outputRatio = add(new MenuComponentMinimalistTextBox(0, 0, 0, 0, "", true).setIntFilter());
    private final MenuComponentMinimalistButton back = add(new MenuComponentMinimalistButton(0, 0, 0, 0, "Back", true, true));
    private final CoolantRecipe coolantRecipe;
    public MenuCoolantRecipeConfiguration(GUI gui, Menu parent, CoolantRecipe coolantRecipe){
        super(gui, parent);
        back.addActionListener((e) -> {
            coolantRecipe.name = name.text;
            coolantRecipe.input = input.text;
            coolantRecipe.output = output.text;
            coolantRecipe.heat = Integer.parseInt(heat.text);
            coolantRecipe.outputRatio = Integer.parseInt(outputRatio.text);
            gui.open(parent);
        });
        this.coolantRecipe = coolantRecipe;
    }
    @Override
    public void onGUIOpened(){
        name.text = coolantRecipe.name;
        input.text = coolantRecipe.input;
        output.text = coolantRecipe.output;
        heat.text = coolantRecipe.heat+"";
        outputRatio.text = coolantRecipe.outputRatio+"";
    }
    @Override
    public void render(int millisSinceLastTick){
        back.width = name.width = Display.getWidth();
        heat.width = input.width = output.width = outputRatio.width = Display.getWidth()*.75;
        heat.x = input.x = output.x = outputRatio.x = Display.getWidth()*.25;
        heat.height = input.height = output.height = outputRatio.height = name.height = back.height = Display.getHeight()/16;
        input.y = name.height;
        output.y = input.y+input.height;
        heat.y = output.y+output.height;
        outputRatio.y = heat.y+heat.height;
        back.y = Display.getHeight()-back.height;
        Core.applyColor(Core.theme.getTextColor());
        drawText(0, Display.getHeight()/16, Display.getWidth()*.25, Display.getHeight()/16*2, "Input");
        drawText(0, Display.getHeight()/16*2, Display.getWidth()*.25, Display.getHeight()/16*3, "Output");
        drawText(0, Display.getHeight()/16*3, Display.getWidth()*.25, Display.getHeight()/16*4, "Heat");
        drawText(0, Display.getHeight()/16*4, Display.getWidth()*.25, Display.getHeight()/16*5, "Output Ratio");
        Core.applyWhite();
        super.render(millisSinceLastTick);
    }
}