package net.ncplanner.plannerator.planner.gui.menu.component.editor;
import java.util.ArrayList;
import net.ncplanner.plannerator.graphics.Renderer;
import net.ncplanner.plannerator.planner.Core;
import net.ncplanner.plannerator.planner.Pinnable;
import net.ncplanner.plannerator.planner.gui.Component;
import net.ncplanner.plannerator.planner.ncpf.configuration.overhaulSFR.CoolantRecipe;
public class MenuComponentCoolantRecipe extends Component implements Pinnable{
    private final CoolantRecipe coolantRecipe;
    public MenuComponentCoolantRecipe(CoolantRecipe coolantRecipe){
        super(0, 0, 0, 0);
        this.coolantRecipe = coolantRecipe;
    }
    @Override
    public void draw(double deltaTime){
        Renderer renderer = new Renderer();
        if(isFocused){
            if(isMouseFocused)renderer.setColor(Core.theme.getMouseoverSelectedComponentColor(Core.getThemeIndex(this)));
            else renderer.setColor(Core.theme.getSelectedComponentColor(Core.getThemeIndex(this)));
        }else{
            if(isMouseFocused)renderer.setColor(Core.theme.getMouseoverComponentColor(Core.getThemeIndex(this)));
            else renderer.setColor(Core.theme.getComponentColor(Core.getThemeIndex(this)));
        }
        renderer.fillRect(x, y, x+width, y+height);
        if(coolantRecipe.getTexture()!=null){
            renderer.setWhite();
            renderer.drawImage(coolantRecipe.getTexture(), x, y, x+height, y+height);
        }
        renderer.setColor(Core.theme.getComponentTextColor(Core.getThemeIndex(this)));
        drawText(renderer);
    }
    public void drawText(Renderer renderer){
        float textLength = renderer.getStringWidth(coolantRecipe.getDisplayName(), height);
        float scale = Math.min(1, (width-(coolantRecipe.getTexture()!=null?height:0))/textLength);
        float textHeight = (int)(height*scale)-1;
        renderer.drawText(coolantRecipe.getTexture()!=null?x+height:x, y+height/2-textHeight/2, x+width, y+height/2+textHeight/2, coolantRecipe.getDisplayName());
    }
    @Override
    public String getTooltip(){
        return "Heat: "+coolantRecipe.stats.heat+"\n"
             + "Output Ratio: "+coolantRecipe.stats.outputRatio;
    }
    @Override
    public ArrayList<String> getSearchableNames(){
        ArrayList<String> lst = coolantRecipe.getSearchableNames();
        for(String s : getTooltip().split("\n"))lst.add(s.trim());
        return lst;
    }
    @Override
    public ArrayList<String> getSimpleSearchableNames(){
        return coolantRecipe.getSimpleSearchableNames();
    }
    @Override
    public String getPinnedName(){
        return coolantRecipe.getPinnedName();
    }
}