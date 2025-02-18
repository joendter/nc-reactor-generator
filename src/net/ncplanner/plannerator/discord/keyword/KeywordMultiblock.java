package net.ncplanner.plannerator.discord.keyword;
import java.util.Locale;
import net.ncplanner.plannerator.discord.Keyword;
import net.ncplanner.plannerator.graphics.image.Color;
import net.ncplanner.plannerator.multiblock.Multiblock;
import net.ncplanner.plannerator.planner.Core;
public class KeywordMultiblock extends Keyword{
    public String text;
    public KeywordMultiblock(){
        super("Multiblock");
    }
    public Multiblock getMultiblock(boolean overhaul){
        String fullMultiblockName = (overhaul?"Overhaul ":"Underhaul ")+text.toUpperCase(Locale.ROOT);
        Multiblock multiblock = null;
        for(Multiblock m : Core.multiblockTypes){
            if(m.getDefinitionName().equalsIgnoreCase(fullMultiblockName))multiblock = m;
        }
        return multiblock;
    }
    @Override
    public boolean doRead(String input){
        text = input.toLowerCase(Locale.ROOT).replace(" ", "").replace("-", "").replace("reactor", "").replace("solidfueled", "sfr").replace("moltensalt", "msr");
        return true;
    }
    @Override
    public Color getColor(){
        return Core.theme.getKeywordColorMultiblock();
    }
    @Override
    public String getRegex(){
        return "turbine|(sfr|msr|solid[ -]?fueled|molten[ -]?salt)( ?reactor)?|(reactor)";
    }
    @Override
    public Keyword newInstance(){
        return new KeywordMultiblock();
    }
    @Override
    public boolean caseSensitive(){
        return false;
    }
}