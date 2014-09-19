package tablesaw;

import tablesaw.interpreters.ScriptInterpreter;
import tablesaw.rules.Rule;

public class ScriptCallAction implements MakeAction
	{
	private ScriptInterpreter m_interpreter;
	private String m_scriptCall;
	
	public ScriptCallAction(ScriptInterpreter interpreter, String scriptCall)
		{
		m_interpreter = interpreter;
		m_scriptCall = scriptCall;
		}
		
	public ScriptCallAction(String scriptCall)
		{
		m_scriptCall = scriptCall;
		Tablesaw make = Tablesaw.getCurrentTablesaw();
		m_interpreter = make.getScriptInterpreter();
		}
	
	public void doMakeAction(Rule cpRule)
			throws TablesawException
		{
		Tablesaw make = Tablesaw.getCurrentTablesaw();
		
		if (m_scriptCall == null)
			return;
			
		Debug.print("Calling %s for %s", m_scriptCall, cpRule);
		m_interpreter.call(m_scriptCall, cpRule);
		}
	}
