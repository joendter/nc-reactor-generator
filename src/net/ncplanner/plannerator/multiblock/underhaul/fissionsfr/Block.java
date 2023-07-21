package net.ncplanner.plannerator.multiblock.underhaul.fissionsfr;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.ncplanner.plannerator.graphics.Renderer;
import net.ncplanner.plannerator.multiblock.Direction;
import net.ncplanner.plannerator.multiblock.Multiblock;
import net.ncplanner.plannerator.multiblock.configuration.IBlockRecipe;
import net.ncplanner.plannerator.multiblock.configuration.ITemplateAccess;
import net.ncplanner.plannerator.ncpf.NCPFConfigurationContainer;
import net.ncplanner.plannerator.planner.Core;
import net.ncplanner.plannerator.planner.MathUtil;
import net.ncplanner.plannerator.planner.StringUtil;
import net.ncplanner.plannerator.planner.ncpf.configuration.UnderhaulSFRConfiguration;
import net.ncplanner.plannerator.planner.ncpf.configuration.underhaulSFR.PlacementRule;
public class Block extends net.ncplanner.plannerator.multiblock.Block implements ITemplateAccess<net.ncplanner.plannerator.planner.ncpf.configuration.underhaulSFR.Block> {
    public net.ncplanner.plannerator.planner.ncpf.configuration.underhaulSFR.Block template;
    public net.ncplanner.plannerator.planner.ncpf.configuration.underhaulSFR.ActiveCoolerRecipe recipe;
    //fuel cell
    public int adjacentCells, adjacentModerators;
    public float energyMult, heatMult;
    //moderator
    public boolean moderatorValid;
    public boolean moderatorActive;
    //cooler
    public boolean coolerValid;
    boolean casingValid;//also for controllers
    public Block(NCPFConfigurationContainer configuration, int x, int y, int z, net.ncplanner.plannerator.planner.ncpf.configuration.underhaulSFR.Block template){
        super(configuration,x,y,z);
        if(template==null)throw new IllegalArgumentException("Cannot create null block!");
        this.template = template;
    }
    @Override
    public net.ncplanner.plannerator.multiblock.Block newInstance(int x, int y, int z){
        return new Block(getConfiguration(), x, y, z, template);
    }
    @Override
    public void copyProperties(net.ncplanner.plannerator.multiblock.Block other){}
    @Override
    public boolean isCore(){
        return isFuelCell()||isModerator();
    }
    public boolean isFuelCell(){
        return template.fuelCell!=null;
    }
    public boolean isModerator(){
        return template.moderator!=null;
    }
    public boolean isCooler(){
        return template.cooler!=null||(template.activeCooler!=null&&recipe!=null);
    }
    public boolean isCasing(){
        return template.casing!=null;
    }
    public boolean isController(){
        return template.controller!=null;
    }
    @Override
    public boolean isActive(){
        return isFuelCell()||moderatorActive||coolerValid||casingValid;
    }
    @Override
    public boolean isValid(){
        return isActive()||moderatorValid;
    }
    public int getCooling(){
        return template.activeCooler==null?template.cooler.cooling:recipe.stats.cooling*getConfiguration().getConfiguration(UnderhaulSFRConfiguration::new).settings.activeCoolerRate/20;
    }
    @Override
    public void clearData(){
        adjacentCells = adjacentModerators = 0;
        energyMult = heatMult = 0;
        moderatorActive = coolerValid = moderatorValid = casingValid = false;
    }
    @Override
    public String getTooltip(Multiblock multiblock){
        String tip = getName();
        if(isController())tip+="\nController "+(casingValid?"Valid":"Invalid");
        if(isCasing())tip+="\nCasing "+(casingValid?"Valid":"Invalid");
        if(isFuelCell()){
            tip+="\n"
                    + " Adjacent Cells: "+adjacentCells+"\n"
                    + " Adjacent Moderators: "+adjacentModerators+"\n"
                    + " Energy Multiplier: "+MathUtil.percent(energyMult, 0)+"\n"
                    + " Heat Multiplier: "+MathUtil.percent(heatMult, 0);
        }
        if(isModerator()){
            tip+="\nModerator "+(moderatorActive?"Active":(moderatorValid?"Valid":"Invalid"));
        }
        if(isCooler()){
            tip+="\nCooler "+(coolerValid?"Valid":"Invalid");
        }
        return tip;
    }
    @Override
    public String getListTooltip(){//TODO auto-generate somehow?
        String tip = getName();
        if(template.fuelCell!=null)tip+="\nFuel Cell";
        if(template.moderator!=null)tip+="\nModerator";
        if(template.cooler!=null){
            tip+="\nCooler"
                + "\nCooling: "+getCooling()+"H/t";
            for(PlacementRule rule : template.cooler.rules){
                tip+="\nRequires "+rule.toTooltipString();
            }
        }
        if(template.activeCooler!=null){
            tip+="\nActive Cooler";
            if(recipe!=null){
                tip+="\nFluid: "+recipe.getDisplayName();
                for(PlacementRule rule : recipe.stats.rules){
                    tip+="\nRequires "+rule.toTooltipString();
                }
            }
        }
        return tip;
    }
    @Override
    public void renderOverlay(Renderer renderer, float x, float y, float z, float width, float height, float depth, Multiblock multiblock, Function<Direction, Boolean> faceRenderFunc){
        if(!isValid()){
            drawOutline(renderer, x, y, z, width, height, depth, Core.theme.getBlockColorOutlineInvalid(), faceRenderFunc);
        }
        if(isActive()&&isModerator()){
            drawOutline(renderer, x, y, z, width, height, depth, Core.theme.getBlockColorOutlineActive(), faceRenderFunc);
        }
    }
    @Override
    public List<PlacementRule> getRules(){
        if(template.cooler!=null)return template.cooler.rules;
        if(recipe!=null)return recipe.stats.rules;
        return new ArrayList<>();
    }
    @Override
    public boolean canRequire(net.ncplanner.plannerator.multiblock.Block oth){
        if(template.cooler!=null||recipe!=null)return requires(oth, null);
        Block other = (Block) oth;
        if(template.fuelCell!=null||template.moderator!=null)return other.template.moderator!=null||other.template.fuelCell!=null;
        return false;
    }
    @Override
    public boolean canGroup(){
        return template.cooler!=null;
    }
    @Override
    public boolean canBeQuickReplaced(){
        return template.cooler!=null;
    }
    @Override
    public boolean defaultEnabled(){
        return template.activeCooler==null;
    }
    @Override
    public Block copy(){
        Block copy = new Block(getConfiguration(), x, y, z, template);
        copy.adjacentCells = adjacentCells;
        copy.adjacentModerators = adjacentModerators;
        copy.energyMult = energyMult;
        copy.heatMult = heatMult;
        copy.moderatorValid = moderatorValid;
        copy.moderatorActive = moderatorActive;
        copy.coolerValid = coolerValid;
        return copy;
    }
    @Override
    public boolean shouldRenderFace(net.ncplanner.plannerator.multiblock.Block against){
        if(super.shouldRenderFace(against))return true;
        if(template==((Block)against).template)return false;
        return Core.hasAlpha(against.getBaseTexture());
    }
    @Override
    public ArrayList<String> getSearchableNames(){
        ArrayList<String> searchables = template.getSearchableNames();
        for(String s : StringUtil.split(getListTooltip(), "\n"))searchables.add(s.trim());
        return searchables;
    }
    @Override
    public ArrayList<String> getSimpleSearchableNames(){
        return template.getSimpleSearchableNames();
    }
    @Override
    public net.ncplanner.plannerator.planner.ncpf.configuration.underhaulSFR.Block getTemplate(){
        return template;
    }
    @Override
    public String getPinnedName(){
        return template.getPinnedName();
    }
    @Override
    public boolean hasRecipes(){
        return template.activeCooler!=null;
    }
    @Override
    public List<? extends IBlockRecipe> getRecipes(){
        return template.activeCoolerRecipes;
    }
}