package planner.menu.component.editor;
import java.util.ArrayList;
import multiblock.Block;
import planner.Core;
import planner.menu.MenuEdit;
import planner.Pinnable;
import simplelibrary.image.Color;
import simplelibrary.opengl.gui.components.MenuComponent;
public class MenuComponentEditorListBlock extends MenuComponent implements Pinnable{
    private final MenuEdit editor;
    public final Block block;
    public MenuComponentEditorListBlock(MenuEdit editor, Block block){
        super(0, 0, 0, 0);
        this.editor = editor;
        this.block = block;
    }
    @Override
    public void render(){
        Color col = isMouseOver?Core.theme.getEditorListBackgroundMouseoverColor(Core.getThemeIndex(this)):Core.theme.getEditorListBackgroundColor(Core.getThemeIndex(this));
        Core.applyColor(col);
        drawRect(x, y, x+width, y+height, 0);
        block.render(x, y, width, height, false, null);
        double border = height/8;
        if(isSelected){
            Core.applyColor(Core.theme.getEditorListLightSelectedColor(Core.getThemeIndex(this)), .85f);
            drawRect(x, y, x+border, y+border, 0);
            drawRect(x+width-border, y, x+width, y+border, 0);
            drawRect(x, y+height-border, x+border, y+height, 0);
            drawRect(x+width-border, y+height-border, x+width, y+height, 0);
            Core.applyColor(Core.theme.getEditorListDarkSelectedColor(Core.getThemeIndex(this)), .85f);
            drawRect(x+border, y, x+width-border, y+border, 0);
            drawRect(x+border, y+height-border, x+width-border, y+height, 0);
            drawRect(x, y+border, x+border, y+height-border, 0);
            drawRect(x+width-border, y+border, x+width, y+height-border, 0);
        }
        if(isMouseOver){
            Core.applyColor(Core.theme.getEditorListLightMouseoverColor(Core.getThemeIndex(this)), .6375f);
            drawRect(x, y, x+border, y+border, 0);
            drawRect(x+width-border, y, x+width, y+border, 0);
            drawRect(x, y+height-border, x+border, y+height, 0);
            drawRect(x+width-border, y+height-border, x+width, y+height, 0);
            Core.applyColor(Core.theme.getEditorListDarkMouseoverColor(Core.getThemeIndex(this)), .6375f);
            drawRect(x+border, y, x+width-border, y+border, 0);
            drawRect(x+border, y+height-border, x+width-border, y+height, 0);
            drawRect(x, y+border, x+border, y+height-border, 0);
            drawRect(x+width-border, y+border, x+width, y+height-border, 0);
        }
    }
    @Override
    public String getTooltip(){
        return block.getListTooltip();
    }
    @Override
    public double getTooltipOffsetX(){
        return 0;
    }
    @Override
    public double getTooltipOffsetY(){
        return height;
    }
    @Override
    public void onMouseButton(double x, double y, int button, boolean pressed, int mods){
        if(button==0&&pressed&&editor.isShiftPressed(0)){
            Pinnable.togglePin(this);
            editor.refreshPartsList();
        }
        else super.onMouseButton(x, y, button, pressed, mods);
    }
    @Override
    public ArrayList<String> getSearchableNames(){
        return block.getSearchableNames();
    }
    @Override
    public ArrayList<String> getSimpleSearchableNames(){
        return block.getSimpleSearchableNames();
    }
    @Override
    public String getPinnedName(){
        return block.getPinnedName();
    }
}