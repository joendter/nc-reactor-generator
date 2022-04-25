package net.ncplanner.plannerator.planner.dssl.token.keyword;
import net.ncplanner.plannerator.planner.dssl.Script;
import net.ncplanner.plannerator.planner.dssl.object.StackFloat;
import net.ncplanner.plannerator.planner.dssl.object.StackInt;
import net.ncplanner.plannerator.planner.dssl.object.StackObject;
public class NegKeyword extends Keyword{
    public NegKeyword(){
        super("neg");
    }
    @Override
    public Keyword newInstance(){
        return new NegKeyword();
    }
    @Override
    public void run(Script script){
        StackObject obj = script.pop();
        if(obj.getBaseType()==StackObject.Type.INT)script.push(new StackInt(obj.asInt().getValue()));
        script.push(new StackFloat(obj.asFloat().getValue()));
    }
}