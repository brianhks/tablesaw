package tablesaw;

import tablesaw.interpreters.ScriptInterpreter;
import tablesaw.rules.Rule;

/**
 Created with IntelliJ IDEA.
 User: bhawkins
 Date: 3/25/14
 Time: 11:11 AM
 To change this template use File | Settings | File Templates.
 */
public class ScriptCallback implements BuildCallback
	{
	private ScriptInterpreter m_interpreter;
	private String m_scriptCall;

	public ScriptCallback(ScriptInterpreter interpreter, String scriptCall)
		{
		m_interpreter = interpreter;
		m_scriptCall = scriptCall;
		}

	public ScriptCallback(String scriptCall)
		{
		m_scriptCall = scriptCall;
		Tablesaw make = Tablesaw.getCurrentTablesaw();
		m_interpreter = make.getScriptInterpreter();
		}

	@Override
	public void doCallback(Object data) throws TablesawException
		{
		Tablesaw make = Tablesaw.getCurrentTablesaw();

		if (m_scriptCall == null)
			return;

		Debug.print("Calling %s for %s", m_scriptCall, data);
		m_interpreter.call(m_scriptCall, data);
		}
	}
