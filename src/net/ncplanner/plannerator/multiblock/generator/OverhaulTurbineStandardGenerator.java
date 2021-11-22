package net.ncplanner.plannerator.multiblock.generator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.ncplanner.plannerator.multiblock.Block;
import net.ncplanner.plannerator.multiblock.CuboidalMultiblock;
import net.ncplanner.plannerator.multiblock.Multiblock;
import net.ncplanner.plannerator.multiblock.Range;
import net.ncplanner.plannerator.multiblock.editor.action.PostProcessingAction;
import net.ncplanner.plannerator.multiblock.editor.action.SetBladeAction;
import net.ncplanner.plannerator.multiblock.editor.action.SetblockAction;
import net.ncplanner.plannerator.multiblock.editor.action.SymmetryAction;
import net.ncplanner.plannerator.multiblock.editor.ppe.PostProcessingEffect;
import net.ncplanner.plannerator.multiblock.editor.symmetry.Symmetry;
import net.ncplanner.plannerator.multiblock.generator.setting.SettingBoolean;
import net.ncplanner.plannerator.multiblock.generator.setting.SettingInt;
import net.ncplanner.plannerator.multiblock.generator.setting.SettingPercentage;
import net.ncplanner.plannerator.multiblock.generator.setting.SettingPostProcessingEffects;
import net.ncplanner.plannerator.multiblock.generator.setting.SettingPriorities;
import net.ncplanner.plannerator.multiblock.generator.setting.SettingSymmetries;
import net.ncplanner.plannerator.multiblock.overhaul.turbine.OverhaulTurbine;
import net.ncplanner.plannerator.planner.exception.MissingConfigurationEntryException;
public class OverhaulTurbineStandardGenerator extends MultiblockGenerator{
    public SettingInt workingMultiblockCount, timeout;
    public SettingPriorities priorities;
    public SettingPercentage changeChance;
    public SettingSymmetries symmetries;
    public SettingPostProcessingEffects postProcessingEffects;
    public SettingBoolean variableRate, lockCore, fillAir;
    private final ArrayList<Multiblock> finalMultiblocks = new ArrayList<>();
    private final ArrayList<Multiblock> workingMultiblocks = new ArrayList<>();
    private int index = 0;
    public OverhaulTurbineStandardGenerator(Multiblock multiblock){
        super(multiblock);
    }
    @Override
    public ArrayList<Multiblock>[] getMultiblockLists(){
        return new ArrayList[]{(ArrayList)finalMultiblocks.clone(),(ArrayList)workingMultiblocks.clone()};
    }
    @Override
    public boolean canGenerateFor(Multiblock multiblock){
        return multiblock instanceof OverhaulTurbine;
    }
    @Override
    public String getName(){
        return "Standard";
    }
    @Override
    protected void createSettings(){
        settings.add(workingMultiblockCount = new SettingInt("Working Multiblocks", 1, 1024, 3, "This is the number of multiblocks that are actively being worked on\nEvery thread will work on all working multiblocks"));
        settings.add(timeout = new SettingInt("Timeout (sec)", 0, 86400, 10, "If a multiblock hasn't changed for this long, it will be reset\nThis is to avoid running into generation dead-ends"));
        settings.add(priorities = new SettingPriorities("Priorities", multiblock.getGenerationPriorities()));
        settings.add(changeChance = new SettingPercentage("Change Chance", .01, "If variable rate is on: Each iteration, each block in the reactor has an x% chance of changing\nIf variable rate is off: Each iteration, exactly x% of the blocks in the reactor will change (minimum of 1)"));
        settings.add(variableRate = new SettingBoolean("Variable Rate", true));
        settings.add(lockCore = new SettingBoolean("Lock Core", false));
        settings.add(fillAir = new SettingBoolean("Fill Air", false));
        settings.add(symmetries = new SettingSymmetries("Symmetry Settings", multiblock.getSymmetries()));
        settings.add(postProcessingEffects = new SettingPostProcessingEffects("Post-Processing", multiblock.getPostProcessingEffects()));
    }
    @Override
    public MultiblockGenerator newInstance(Multiblock multi){
        return new OverhaulTurbineStandardGenerator(multi);
    }
    @Override
    public void tick(){
        int size;
        //<editor-fold defaultstate="collapsed" desc="Adding/removing working multiblocks">
        synchronized(workingMultiblocks){
            size = workingMultiblocks.size();
        }
        if(size<workingMultiblockCount.getValue()){
            OverhaulTurbine inst = (OverhaulTurbine)multiblock.blankCopy();
            ArrayList<Integer> diameters = new ArrayList<>();
            for(int i = inst.getMinBearingDiameter(); i<=inst.getMaxBearingDiameter(); i+=2)diameters.add(i);
            inst.setBearing(diameters.get(rand.nextInt(diameters.size())));
            inst.buildDefaultCasing();
            inst.recalculate();
            synchronized(workingMultiblocks){
                workingMultiblocks.add(inst);
            }
        }else if(size>workingMultiblockCount.getValue()){
            synchronized(workingMultiblocks){
                Multiblock worst = null;
                for(Multiblock mb : workingMultiblocks){
                    if(worst==null||worst.isBetterThan(mb, priorities.getValue())){
                        worst = mb;
                    }
                }
                if(worst!=null){
                    finalize(worst);
                    workingMultiblocks.remove(worst);
                }
            }
        }
//</editor-fold>
        CuboidalMultiblock currentMultiblock = null;
        int idx = index;
        //<editor-fold defaultstate="collapsed" desc="Fetch Current multiblock">
        synchronized(workingMultiblocks){
            if(idx>=workingMultiblocks.size())idx = 0;
            if(!workingMultiblocks.isEmpty()){
                currentMultiblock = (CuboidalMultiblock)workingMultiblocks.get(idx).copy();
            }
            index++;
            if(index>=workingMultiblocks.size())index = 0;
        }
//</editor-fold>
        if(currentMultiblock==null)return;//there's nothing to do!
        if(variableRate.getValue()){
            if(rand.nextBoolean()){//change coil
                for(int x = 1; x<=currentMultiblock.getInternalWidth(); x++){
                    for(int y = 1; y<=currentMultiblock.getInternalHeight(); y++){
                        for(int z = 0; z<2; z++){
                            if(z==1)z = currentMultiblock.getExternalDepth()-1;
                            Block b = currentMultiblock.getBlock(x, y, z);
                            if(lockCore.getValue()&&b!=null&&b.isCore())continue;
                            if(rand.nextDouble()<changeChance.getValue()||(fillAir.getValue()&&b==null)){
                                Block randBlock = randCoil(currentMultiblock, getAllowedBlocks());
                                if(randBlock==null||lockCore.getValue()&&randBlock.isCore())continue;//nope
                                currentMultiblock.queueAction(new SetblockAction(x, y, z, applyMultiblockSpecificSettings(currentMultiblock, randBlock.newInstance(x, y, z))));
                            }
                        }
                    }
                }
            }else{
                //change blade
                int x = ((CuboidalMultiblock)multiblock).getExternalWidth()/2;
                int y = 0;
                for(int z = 1; z<currentMultiblock.getExternalDepth()-1; z++){
                    Block b = currentMultiblock.getBlock(x, y, z);
                    if(lockCore.getValue()&&b!=null&&b.isCore())continue;
                    if(rand.nextDouble()<changeChance.getValue()||(fillAir.getValue()&&b==null)){
                        Block randBlock = randBlade(currentMultiblock, getAllowedBlocks());
                        if(randBlock==null||lockCore.getValue()&&randBlock.isCore())continue;//nope
                        currentMultiblock.queueAction(new SetBladeAction(((OverhaulTurbine)currentMultiblock).bearingDiameter, z, (net.ncplanner.plannerator.multiblock.overhaul.turbine.Block)applyMultiblockSpecificSettings(currentMultiblock, randBlock.newInstance(x, y, z))));
                    }
                }
            }
        }else{
            int changes = (int) Math.max(1, Math.round(changeChance.getValue()*currentMultiblock.getTotalVolume()));
            ArrayList<int[]> pool = new ArrayList<>();
                for(int x = 1; x<=currentMultiblock.getInternalWidth(); x++){
                    for(int y = 1; y<=currentMultiblock.getInternalHeight(); y++){
                    for(int z = 0; z<2; z++){
                        if(z==1)z = currentMultiblock.getExternalDepth()-1;
                        if(fillAir.getValue()&&currentMultiblock.getBlock(x, y, z)==null){
                            Block randBlock = randCoil(currentMultiblock, getAllowedBlocks());
                            if(randBlock==null||lockCore.getValue()&&randBlock.isCore())continue;//nope
                            currentMultiblock.queueAction(new SetblockAction(x, y, z, applyMultiblockSpecificSettings(currentMultiblock, randBlock.newInstance(x, y, z))));
                            continue;
                        }
                        pool.add(new int[]{x,y,z});
                    }
                }
            }
            int x = ((CuboidalMultiblock)multiblock).getExternalWidth()/2;
            int y = 0;
            for(int z = 1; z<currentMultiblock.getExternalDepth()-1; z++){
                if(fillAir.getValue()&&currentMultiblock.getBlock(x, y, z)==null){
                    Block randBlock = randBlade(currentMultiblock, getAllowedBlocks());
                    if(randBlock==null||lockCore.getValue()&&randBlock.isCore())continue;//nope
                    currentMultiblock.queueAction(new SetBladeAction(((OverhaulTurbine)currentMultiblock).bearingDiameter, z, (net.ncplanner.plannerator.multiblock.overhaul.turbine.Block)applyMultiblockSpecificSettings(currentMultiblock, randBlock.newInstance(x, y, z))));
                    continue;
                }
                pool.add(new int[]{x,y,z});
            }
            for(int i = 0; i<changes; i++){//so it can't change the same cell twice
                if(pool.isEmpty())break;
                int[] pos = pool.remove(rand.nextInt(pool.size()));
                Block b = currentMultiblock.getBlock(pos[0], pos[1], pos[2]);
                if(lockCore.getValue()&&b!=null&&b.isCore())continue;
                Block randBlock = (pos[2]==0||pos[2]==currentMultiblock.getExternalDepth()-1)?randCoil(currentMultiblock, getAllowedBlocks()):randBlade(currentMultiblock, getAllowedBlocks());
                if(randBlock==null||lockCore.getValue()&&randBlock.isCore())continue;//nope
                currentMultiblock.queueAction(((net.ncplanner.plannerator.multiblock.overhaul.turbine.Block)randBlock).template.blade?new SetBladeAction(((OverhaulTurbine)currentMultiblock).bearingDiameter, pos[2], (net.ncplanner.plannerator.multiblock.overhaul.turbine.Block)applyMultiblockSpecificSettings(currentMultiblock, randBlock.newInstance(0, 0, 0))):new SetblockAction(pos[0], pos[1], pos[2], applyMultiblockSpecificSettings(currentMultiblock, randBlock.newInstance(pos[0], pos[1], pos[2]))));
            }
        }
        currentMultiblock.performActions(false);
        for(PostProcessingEffect effect : postProcessingEffects.getValue()){
            if(effect.preSymmetry)currentMultiblock.action(new PostProcessingAction(effect, this), true, false);
        }
        for(Symmetry symmetry : symmetries.getValue()){
            currentMultiblock.queueAction(new SymmetryAction(symmetry));
        }
        currentMultiblock.performActions(false);
        currentMultiblock.recalculate();
        for(PostProcessingEffect effect : postProcessingEffects.getValue()){
            if(effect.postSymmetry)currentMultiblock.action(new PostProcessingAction(effect, this), true, false);
        }
        currentMultiblock.buildDefaultCasing();
        currentMultiblock.recalculate();
        synchronized(workingMultiblocks.get(idx)){
            Multiblock mult = workingMultiblocks.get(idx);
            finalize(mult);
            if(currentMultiblock.isBetterThan(mult, priorities.getValue())){workingMultiblocks.set(idx, currentMultiblock.copy());}
            else if(mult.millisSinceLastChange()>timeout.getValue()*1000){
                OverhaulTurbine m = (OverhaulTurbine)multiblock.blankCopy();
                ArrayList<Integer> diameters = new ArrayList<>();
                for(int i = m.getMinBearingDiameter(); i<=m.getMaxBearingDiameter(); i+=2)diameters.add(i);
                m.setBearing(diameters.get(rand.nextInt(diameters.size())));
                m.buildDefaultCasing();
                m.recalculate();
                workingMultiblocks.set(idx, m);
            }
        }
        countIteration();
    }
    private Block applyMultiblockSpecificSettings(Multiblock currentMultiblock, Block randBlock){
        if(multiblock instanceof OverhaulTurbine)return randBlock;//also no block-specifics!
        throw new IllegalArgumentException("Unknown multiblock: "+multiblock.getDefinitionName());
    }
    private void finalize(Multiblock worst){
        if(worst==null)return;
        synchronized(finalMultiblocks){
        //<editor-fold defaultstate="collapsed" desc="Adding/removing final multiblocks">
            int finalMultiblockCount = 1;//not gonna bother making it modifiable
            if(finalMultiblocks.size()<finalMultiblockCount){
                finalMultiblocks.add(worst.copy());
                return;
            }else if(finalMultiblocks.size()>finalMultiblockCount){
                Multiblock wrst = null;
                for(Multiblock mb : finalMultiblocks){
                    if(wrst==null||wrst.isBetterThan(mb, priorities.getValue())){
                        wrst = mb;
                    }
                }
                if(wrst!=null){
                    finalMultiblocks.remove(wrst);
                }
            }
//</editor-fold>
            for(int i = 0; i<finalMultiblocks.size(); i++){
                Multiblock multi = finalMultiblocks.get(i);
                if(worst.isBetterThan(multi, priorities.getValue())){
                    finalMultiblocks.set(i, worst.copy());
                    return;
                }
            }
        }
    }
    @Override
    public void importMultiblock(Multiblock multiblock) throws MissingConfigurationEntryException{
        if(!multiblock.isShapeEqual(this.multiblock))return;
        multiblock.convertTo(this.multiblock.getConfiguration());
        for(Range<Block> range : getAllowedBlocks()){
            for(Block block : ((Multiblock<Block>)multiblock).getBlocks()){
                if(multiblock.count(block)>range.max)multiblock.action(new SetblockAction(block.x, block.y, block.z, null), true, false);
            }
        }
        ALLOWED:for(Block block : ((Multiblock<Block>)multiblock).getBlocks()){
            for(Range<Block> range : getAllowedBlocks()){
                if(range.obj.isEqual(block))continue ALLOWED;
            }
            multiblock.action(new SetblockAction(block.x, block.y, block.z, null), true, false);
        }
        finalize(multiblock);
        workingMultiblocks.add(multiblock.copy());
    }
    private Block randBlade(Multiblock multiblock, List<Range<Block>> ranges){
        if(ranges.isEmpty())return null;
        ranges = new ArrayList<>(ranges);
        for(Iterator<Range<Block>> it = ranges.iterator(); it.hasNext();){
            Range<Block> next = it.next();
            if(!((net.ncplanner.plannerator.multiblock.overhaul.turbine.Block)next.obj).template.blade)it.remove();
        }
        for(Range<Block> range : ranges){
            if(range.min==0&&range.max==Integer.MAX_VALUE)continue;
            if(multiblock.count(range.obj)<range.min)return range.obj;
        }
        Range<Block> randRange = ranges.get(rand.nextInt(ranges.size()));
        if((randRange.min!=0||randRange.max!=Integer.MAX_VALUE)&&randRange.max!=0&&multiblock.count(randRange.obj)>=randRange.max){
            return null;
        }
        return randRange.obj;
    }
    private Block randCoil(Multiblock multiblock, List<Range<Block>> ranges){
        if(ranges.isEmpty())return null;
        ranges = new ArrayList<>(ranges);
        for(Iterator<Range<Block>> it = ranges.iterator(); it.hasNext();){
            Range<Block> next = it.next();
            if(!((net.ncplanner.plannerator.multiblock.overhaul.turbine.Block)next.obj).template.coil&&!((net.ncplanner.plannerator.multiblock.overhaul.turbine.Block)next.obj).template.connector)it.remove();
        }
        for(Range<Block> range : ranges){
            if(range.min==0&&range.max==Integer.MAX_VALUE)continue;
            if(multiblock.count(range.obj)<range.min)return range.obj;
        }
        Range<Block> randRange = ranges.get(rand.nextInt(ranges.size()));
        if((randRange.min!=0||randRange.max!=Integer.MAX_VALUE)&&randRange.max!=0&&multiblock.count(randRange.obj)>=randRange.max){
            return null;
        }
        return randRange.obj;
    }
    private <T extends Object> T rand(Multiblock multiblock, List<Range<T>> ranges){
        if(ranges.isEmpty())return null;
        for(Range<T> range : ranges){
            if(range.min==0&&range.max==Integer.MAX_VALUE)continue;
            if(multiblock.count(range.obj)<range.min)return range.obj;
        }
        Range<T> randRange = ranges.get(rand.nextInt(ranges.size()));
        if((randRange.min!=0||randRange.max!=Integer.MAX_VALUE)&&randRange.max!=0&&multiblock.count(randRange.obj)>=randRange.max){
            return null;
        }
        return randRange.obj;
    }
}