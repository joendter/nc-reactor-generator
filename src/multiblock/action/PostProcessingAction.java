package multiblock.action;
import java.util.ArrayList;
import multiblock.Action;
import multiblock.Block;
import multiblock.Multiblock;
import multiblock.ppe.PostProcessingEffect;
public class PostProcessingAction extends Action<Multiblock>{
    private final PostProcessingEffect postProcessingEffect;
    private Block[][][] was;
    public PostProcessingAction(PostProcessingEffect postProcessingEffect){
        this.postProcessingEffect = postProcessingEffect;
    }
    @Override
    public void doApply(Multiblock multiblock){
        was = new Block[multiblock.getX()][multiblock.getY()][multiblock.getZ()];
        for(int x = 0; x<multiblock.getX(); x++){
            for(int y = 0; y<multiblock.getY(); y++){
                for(int z = 0; z<multiblock.getZ(); z++){
                    was[x][y][z] = multiblock.blocks[x][y][z];
                }
            }
        }
        postProcessingEffect.apply(multiblock);
    }
    @Override
    public void doUndo(Multiblock multiblock){
        for(int x = 0; x<multiblock.getX(); x++){
            for(int y = 0; y<multiblock.getY(); y++){
                for(int z = 0; z<multiblock.getZ(); z++){
                    multiblock.blocks[x][y][z] = was[x][y][z];
                }
            }
        }
    }
    @Override
    protected void getAffectedBlocks(Multiblock multiblock, ArrayList<Block> blocks){
        blocks.addAll(multiblock.getBlocks());//TODO only list the actually affected blocks
    }
}