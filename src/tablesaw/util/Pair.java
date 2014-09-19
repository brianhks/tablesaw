package tablesaw.util;

public class Pair<A, B>
	{
	A m_firstMember;
	B m_secondMember;
	
	public Pair()
		{
		m_firstMember = null;
		m_secondMember = null;
		}
		
	public Pair(A first, B second)
		{
		m_firstMember = first;
		m_secondMember = second;
		}
		
	public A getFirst()
		{
		return (m_firstMember);
		}
		
	public void setFirst(A first)
		{
		m_firstMember = first;
		}
		
	public B getSecond()
		{
		return (m_secondMember);
		}
		
	public void setSecond(B second)
		{
		m_secondMember = second;
		}
		
	@Override
	public int hashCode()
		{
		return (m_firstMember.hashCode() + m_secondMember.hashCode());
		}
		
  @SuppressWarnings("unchecked")
	public boolean equals(Object other)
		{
		if (!(other instanceof Pair))
			return (false);
			
		Pair<Object, Object> that = (Pair<Object, Object>)other;
		
		return (m_firstMember.equals(that.m_firstMember) && 
				m_secondMember.equals(that.m_secondMember));
		}
	}
