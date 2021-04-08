package planner.menu;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import multiblock.Multiblock;
import multiblock.configuration.Configuration;
import planner.Core;
import planner.Theme;
import planner.file.FileFormat;
import planner.file.FileReader;
import planner.file.NCPFFile;
import planner.menu.component.MenuComponentLabel;
import planner.menu.component.MenuComponentMinimaList;
import planner.menu.component.MenuComponentMinimalistButton;
import planner.menu.component.MenuComponentMinimalistOptionButton;
import planner.menu.component.MenuComponentToggleBox;
import planner.menu.configuration.MenuConfiguration;
import planner.module.Module;
import simplelibrary.Sys;
import simplelibrary.config2.Config;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.opengl.gui.GUI;
import simplelibrary.opengl.gui.Menu;
public class MenuSettings extends SettingsMenu{
    private final MenuComponentLabel quickLoadLabel = add(new MenuComponentLabel(0, 0, 0, 48, "Internal Configurations", true));
    private final MenuComponentMinimaList quickLoadList = add(new MenuComponentMinimaList(0, 0, 0, 0, 32));
    private final MenuComponentLabel currentConfigLabel = add(new MenuComponentLabel(0, 0, 0, 48, "Current Configuration", true));
    private final MenuComponentMinimalistButton load = add(new MenuComponentMinimalistButton(0, 0, 0, 48, "Load", true, true).setTooltip("Load configuration from a file, replacing the current configuration\nAny existing multiblocks will be converted to the new configuration\nYou can load the following files:\nnuclearcraft.cfg in the game files\nany .ncpf configuration file"));
    private final MenuComponentMinimalistButton save = add(new MenuComponentMinimalistButton(0, 0, 0, 48, "Save", true, true).setTooltip("Save the configuration to a .ncpf file"));
    private final MenuComponentMinimalistButton modify = add(new MenuComponentMinimalistButton(0, 0, 0, 48, "Modify", true, true).setTooltip("Modify the current configuration"));
    private final MenuComponentMinimalistOptionButton theme;
    private final MenuComponentMinimalistButton modules;
    private final MenuComponentToggleBox invertUndoRedo;
    private final MenuComponentToggleBox autoBuildCasing;
    public MenuSettings(GUI gui, Menu parent){
        super(gui, parent);
        addToSidebar(new MenuComponentLabel(0, 0, 0, 48, "Settings", true));
        addToSidebar(invertUndoRedo = new MenuComponentToggleBox(0, 0, 0, 48, "Invert Undo/Redo", false));
        addToSidebar(autoBuildCasing = new MenuComponentToggleBox(0, 0, 0, 48, "Auto-build Casings", false));
        addToSidebar(modules = new MenuComponentMinimalistButton(0, 0, 0, 48, "Modules", true, true));
        modules.addActionListener((e) -> {
            gui.open(new MenuModules(gui, this));
        });
        MenuComponentMinimalistButton tutorials;
        addToSidebar(tutorials = new MenuComponentMinimalistButton(0, 0, 0, 48, "Tutorials", true, true));
        tutorials.addActionListener((e) -> {
            gui.open(new MenuTransition(gui, this, new MenuTutorial(gui, this), MenuTransition.SplitTransitionX.slideIn(300d/gui.helper.displayWidth()), 4));
        });
        theme = addToSidebar(new MenuComponentMinimalistOptionButton(0, 0, 0, 48, "Theme", true, true, Theme.themes.indexOf(Core.theme), Theme.getThemeS()).setTooltip("Click to cycle through available themes\nRight click to cycle back"));
        for(Configuration config : Configuration.configurations){
            MenuComponentMinimalistButton b = new MenuComponentMinimalistButton(0, 0, 0, 48, "Load "+config.toString(), true, true).setTooltip("Replace the current configuration with "+config.toString()+"\nAll multiblocks will be converted to the new configuration");
            b.addActionListener((e) -> {
                config.impose(Core.configuration);
                for(Multiblock multi : Core.multiblocks){
                    multi.convertTo(Core.configuration);
                }
                onGUIOpened();
            });
            quickLoadList.add(b);
        }
        load.addActionListener((e) -> {
            Core.createFileChooser((file, format) -> {
                NCPFFile ncpf = FileReader.read(file);
                if(ncpf==null)return;
                Configuration.impose(ncpf.configuration, Core.configuration);
                for(Multiblock multi : Core.multiblocks){
                    multi.convertTo(Core.configuration);
                }
                onGUIOpened();
            }, FileFormat.ALL_CONFIGURATION_FORMATS);
        });
        save.addActionListener((e) -> {
            Core.createFileChooser(new File(Core.configuration.getFullName()), (file, format) -> {
                if(!file.getName().endsWith(".ncpf"))file = new File(file.getAbsolutePath()+".ncpf");
                file = Core.askForOverwrite(file);
                if(file==null)return;
                try(FileOutputStream stream = new FileOutputStream(file)){
                    Config header = Config.newConfig();
                    header.set("version", NCPFFile.SAVE_VERSION);
                    header.set("count", 0);
                    header.save(stream);
                    Core.configuration.save(null, Config.newConfig()).save(stream);
                }catch(IOException ex){
                    Sys.error(ErrorLevel.severe, "Failed to save configuration!", ex, ErrorCategory.fileIO);
                }
            }, FileFormat.NCPF);
        });
        modify.addActionListener((e) -> {
            gui.open(new MenuTransition(gui, this, new MenuConfiguration(gui, this, Core.configuration), MenuTransition.SplitTransitionX.slideIn(sidebar.width/gui.helper.displayWidth()), 4));
        });
    }
    @Override
    public void onGUIOpened(){
        invertUndoRedo.isToggledOn = Core.invertUndoRedo;
        autoBuildCasing.isToggledOn = Core.autoBuildCasing;
        currentConfigLabel.text = "Current Configuration: "+Core.configuration.toString();
        int active = 0;
        for(Module m : Core.modules)if(m.isActive())active++;
        modules.label = "Modules ("+active+"/"+Core.modules.size()+" Active)";
    }
    @Override
    public void onGUIClosed(){
        Core.invertUndoRedo = invertUndoRedo.isToggledOn;
        Core.autoBuildCasing = autoBuildCasing.isToggledOn;
        super.onGUIClosed();
    }
    @Override
    public void render(int millisSinceLastTick){
        quickLoadLabel.width = quickLoadList.width = currentConfigLabel.width = gui.helper.displayWidth()-sidebar.width;
        modify.width = load.width = save.width = currentConfigLabel.width/3;
        quickLoadLabel.x = quickLoadList.x = currentConfigLabel.x = modify.x = load.x = sidebar.width;
        save.x = load.x+load.width;
        modify.x = save.x+save.width;
        save.x+=3;
        modify.x+=6;
        modify.width-=6;
        quickLoadList.y = quickLoadLabel.height;
        quickLoadList.height = gui.helper.displayHeight()/3-quickLoadLabel.height;
        currentConfigLabel.y = quickLoadList.y+quickLoadList.height;
        save.y = load.y = modify.y = currentConfigLabel.y+currentConfigLabel.height;
        if(Theme.themes.indexOf(Core.theme)!=theme.getIndex()){
            try{
                Core.setTheme(Theme.themes.get(theme.getIndex()));
            }catch(IndexOutOfBoundsException ex){
                gui.open(new MenuSettings(gui, (Menu)parent));
            }
        }
        super.render(millisSinceLastTick);
    }
    
}