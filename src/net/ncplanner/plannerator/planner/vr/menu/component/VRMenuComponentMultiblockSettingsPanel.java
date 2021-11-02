package net.ncplanner.plannerator.planner.vr.menu.component;
import net.ncplanner.plannerator.Renderer;
import net.ncplanner.plannerator.multiblock.Multiblock;
import net.ncplanner.plannerator.multiblock.overhaul.fissionsfr.OverhaulSFR;
import net.ncplanner.plannerator.multiblock.overhaul.fusion.OverhaulFusionReactor;
import net.ncplanner.plannerator.multiblock.overhaul.turbine.OverhaulTurbine;
import net.ncplanner.plannerator.multiblock.underhaul.fissionsfr.UnderhaulSFR;
import net.ncplanner.plannerator.planner.Core;
import net.ncplanner.plannerator.planner.vr.VRMenuComponent;
import net.ncplanner.plannerator.planner.vr.menu.VRMenuEdit;
import org.lwjgl.openvr.TrackedDevicePose;
public class VRMenuComponentMultiblockSettingsPanel extends VRMenuComponent{
    private final VRMenuEdit editor;
    private boolean refreshNeeded = true;
    public VRMenuComponentMultiblockSettingsPanel(VRMenuEdit editor, double x, double y, double z, double width, double height, double depth, double rx, double ry, double rz){
        super(x, y, z, width, height, depth, rx, ry, rz);
        this.editor = editor;
    }
    @Override
    public void renderComponent(Renderer renderer, TrackedDevicePose.Buffer tdpb){
        renderer.setColor(Core.theme.getVRPanelOutlineColor());
        renderer.drawCubeOutline(-.005, -.005, -.005, width+.005, height+.005, depth+.005, .005);//half cm
    }
    @Override
    public void tick(){
        super.tick();
        if(refreshNeeded)refresh();
    }
    public synchronized void refresh(){
        components.clear();
        Multiblock multiblock = editor.getMultiblock();
        if(multiblock instanceof UnderhaulSFR){
            double size = Math.min(depth, height/multiblock.getConfiguration().underhaul.fissionSFR.allFuels.size());
            for(int i = 0; i<multiblock.getConfiguration().underhaul.fissionSFR.allFuels.size(); i++){
                net.ncplanner.plannerator.multiblock.configuration.underhaul.fissionsfr.Fuel fuel = multiblock.getConfiguration().underhaul.fissionSFR.allFuels.get(i);
                add(new VRMenuComponentUnderFuel(editor, 0, height-size*(i+1), 0, width, size, depth, fuel));
            }
        }
        if(multiblock instanceof OverhaulSFR){
            double size = Math.min(depth, height/multiblock.getConfiguration().overhaul.fissionSFR.allCoolantRecipes.size());
            for(int i = 0; i<multiblock.getConfiguration().overhaul.fissionSFR.allCoolantRecipes.size(); i++){
                net.ncplanner.plannerator.multiblock.configuration.overhaul.fissionsfr.CoolantRecipe recipe = multiblock.getConfiguration().overhaul.fissionSFR.allCoolantRecipes.get(i);
                add(new VRMenuComponentCoolantRecipe(editor, 0, height-size*(i+1), 0, width, size, depth, recipe));
            }
        }
        if(multiblock instanceof OverhaulTurbine){
            double size = Math.min(depth, height/multiblock.getConfiguration().overhaul.turbine.allRecipes.size());
            for(int i = 0; i<multiblock.getConfiguration().overhaul.turbine.allRecipes.size(); i++){
                net.ncplanner.plannerator.multiblock.configuration.overhaul.turbine.Recipe recipe = multiblock.getConfiguration().overhaul.turbine.allRecipes.get(i);
                add(new VRMenuComponentTurbineRecipe(editor, 0, height-size*(i+1), 0, width, size, depth, recipe));
            }
        }
        if(multiblock instanceof OverhaulFusionReactor){
            double size = Math.min(depth, height/multiblock.getConfiguration().overhaul.fusion.allRecipes.size());
            for(int i = 0; i<multiblock.getConfiguration().overhaul.fusion.allRecipes.size(); i++){
                net.ncplanner.plannerator.multiblock.configuration.overhaul.fusion.Recipe recipe = multiblock.getConfiguration().overhaul.fusion.allRecipes.get(i);
                add(new VRMenuComponentFusionRecipe(editor, 0, height-size*(i+1), 0, width/2, size, depth, recipe));
            }
            size = Math.min(depth, height/multiblock.getConfiguration().overhaul.fusion.allCoolantRecipes.size());
            for(int i = 0; i<multiblock.getConfiguration().overhaul.fusion.allCoolantRecipes.size(); i++){
                net.ncplanner.plannerator.multiblock.configuration.overhaul.fusion.CoolantRecipe recipe = multiblock.getConfiguration().overhaul.fusion.allCoolantRecipes.get(i);
                add(new VRMenuComponentFusionCoolantRecipe(editor, width/2, height-size*(i+1), 0, width/2, size, depth, recipe));
            }
        }
        refreshNeeded = false;
    }
}