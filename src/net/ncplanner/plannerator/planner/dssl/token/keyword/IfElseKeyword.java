package net.ncplanner.plannerator.planner.dssl.token.keyword;
public class IfElseKeyword extends Keyword{
    public IfElseKeyword(){
        super("ifelse");
    }
    @Override
    public Keyword newInstance(){
        return new IfElseKeyword();
    }
    @Override
    public KeywordFlavor getFlavor(){
        return KeywordFlavor.FLOW;
    }
}