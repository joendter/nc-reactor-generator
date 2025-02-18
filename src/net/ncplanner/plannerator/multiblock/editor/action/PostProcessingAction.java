package net.ncplanner.plannerator.multiblock.editor.action;
import java.util.ArrayList;
import java.util.HashMap;
import net.ncplanner.plannerator.multiblock.AbstractBlock;
import net.ncplanner.plannerator.multiblock.BlockPos;
import net.ncplanner.plannerator.multiblock.Multiblock;
import net.ncplanner.plannerator.multiblock.editor.Action;
import net.ncplanner.plannerator.multiblock.editor.ppe.PostProcessingEffect;
import net.ncplanner.plannerator.multiblock.generator.MultiblockGenerator;
public class PostProcessingAction extends Action<Multiblock>{
    private final PostProcessingEffect postProcessingEffect;
    private HashMap<BlockPos, AbstractBlock> was = new HashMap<>();
    private final MultiblockGenerator generator;
    public PostProcessingAction(PostProcessingEffect postProcessingEffect, MultiblockGenerator generator){
        this.postProcessingEffect = postProcessingEffect;
        this.generator = generator;
    }
    @Override
    public void doApply(Multiblock multiblock, boolean allowUndo){
        if(allowUndo){
            multiblock.forEachPosition((x, y, z) -> {
                was.put(new BlockPos(x,y,z), multiblock.getBlock(x, y, z));
            });
        }
        postProcessingEffect.apply(multiblock, generator);
    }
    @Override
    public void doUndo(Multiblock multiblock){
        for(BlockPos pos : was.keySet()){
            multiblock.setBlockExact(pos.x, pos.y, pos.z, was.get(pos));
        }
    }
    @Override
    public void getAffectedBlocks(Multiblock multiblock, ArrayList<AbstractBlock> blocks){
        //TODO only list the actually affected blocks
    }
}