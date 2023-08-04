package net.ncplanner.plannerator.planner.gui.menu.dialog;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import net.ncplanner.plannerator.graphics.Renderer;
import net.ncplanner.plannerator.multiblock.Multiblock;
import net.ncplanner.plannerator.planner.Core;
import net.ncplanner.plannerator.planner.exception.MissingConfigurationEntryException;
import net.ncplanner.plannerator.planner.file.FileFormat;
import net.ncplanner.plannerator.planner.file.FileReader;
import net.ncplanner.plannerator.planner.file.LegacyNCPFFile;
import net.ncplanner.plannerator.planner.gui.GUI;
import net.ncplanner.plannerator.planner.gui.Menu;
import net.ncplanner.plannerator.planner.gui.menu.component.Button;
import net.ncplanner.plannerator.planner.gui.menu.component.Label;
import net.ncplanner.plannerator.planner.gui.menu.component.layout.GridLayout;
import net.ncplanner.plannerator.planner.ncpf.Design;
import net.ncplanner.plannerator.planner.ncpf.Project;
import net.ncplanner.plannerator.planner.ncpf.design.MultiblockDesign;
public class MenuImport extends MenuDialog{
    public MenuImport(GUI gui, Menu parent){
        super(gui, parent);
        addButton("Cancel", () -> {
            close();
        });
        addButton("System File Chooser", () -> {
            try{
                Core.createFileChooser((file) -> {
                    Thread t = new Thread(() -> {
                        importMultiblocks(file);
                        close();
                    });
                    t.setDaemon(true);
                    t.start();
                }, FileFormat.ALL_PLANNER_FORMATS);
            }catch(IOException ex){
                Core.error("Failed to import file!", ex);
            }
        });
        refresh();
    }
    @Override
    public void onOpened(){
        refresh();
    }
    private void refresh(){
        GridLayout layout = new GridLayout(36, 1);
        File root = new File("file").getAbsoluteFile().getParentFile();
        for(File file : root.listFiles()){
            String filename = file.getName();
            if(filename.endsWith(".ncpf")||filename.endsWith(".json")){
                Label mainLabel = layout.add(new Label(0, 0, 0, 36, filename, true){
                    Button del = add(new Button(0, 0, 64, height, "Del", true));
                    Button imp = add(new Button(0, 0, 96, height, "Import", true));
                    {
                        del.addAction(() -> {
                            new MenuDialog(MenuImport.this.gui, MenuImport.this){
                                {
                                    textBox.setText("Delete "+filename+"?");
                                    addButton("Cancel", () -> {
                                        close();
                                    });
                                    addButton("Delete", () -> {
                                        try{
                                            Files.delete(file.toPath());
                                            close();
                                            MenuImport.this.refresh();
                                        }catch(IOException ex){
                                            Core.error("Failed to delete file!", ex);
                                        }
                                    });
                                }
                            }.open();
                        });
                        imp.addAction(() -> {
                            Thread t = new Thread(() -> {
                                importMultiblocks(file);
                                close();
                            });
                            t.setDaemon(true);
                            t.start();
                        });
                    }
                    @Override
                    public void render2d(double deltaTime){
                        del.x = width-del.width;
                        imp.x = del.x-imp.width;
                        super.render2d(deltaTime);
                    }
                    @Override
                    public void drawText(Renderer renderer){
                        float textLength = renderer.getStringWidth(text, height);
                        float scale = Math.min(1, (width-del.width-imp.width-textInset*2)/textLength);
                        float textHeight = (int)((height-textInset*2)*scale)-4;
                        renderer.drawCenteredText(x, y+height/2-textHeight/2, x+width-del.width-imp.width, y+height/2+textHeight/2, text);
                    }
                });
                layout.add(new Label(0, 0, 0, 0, "", true));
            }
        }
        maxWidth = 0.75f;
        layout.width = gui.getWidth()*2/5;
        setContent(layout);
    }
    private void importMultiblocks(File file){
        Project project = FileReader.read(file);
        if(project==null)return;
        //TODO configuration matches?
        for(Design design : project.designs){
            Design copy = design.copyTo(()->Design.registeredDesigns.get(design.definition.type).apply(Core.project));//copy to new configuration, should(tm) set all the references properly, right?
            if(copy instanceof MultiblockDesign){
                Core.multiblocks.add(((MultiblockDesign)copy).toMultiblock());
            }
        }
    }
}