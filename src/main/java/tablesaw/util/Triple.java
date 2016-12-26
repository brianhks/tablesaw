package tablesaw.util;

/**
 Created by bhawkins on 3/25/14.
 */
public class Triple<A, B, C>
	{
	A m_first;
	B m_second;
	C m_third;

	public Triple()
		{
		}

	public Triple(A first, B second, C third)
		{
		m_first = first;
		m_second = second;
		m_third = third;
		}

	public A getFirst()
		{
		return m_first;
		}

	public void setFirst(A first)
		{
		m_first = first;
		}

	public B getSecond()
		{
		return m_second;
		}

	public void setSecond(B second)
		{
		m_second = second;
		}

	public C getThird()
		{
		return m_third;
		}

	public void setThird(C third)
		{
		m_third = third;
		}

	@Override
	public boolean equals(Object o)
		{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Triple triple = (Triple) o;

		if (m_first != null ? !m_first.equals(triple.m_first) : triple.m_first != null)
			return false;
		if (m_second != null ? !m_second.equals(triple.m_second) : triple.m_second != null)
			return false;
		if (m_third != null ? !m_third.equals(triple.m_third) : triple.m_third != null)
			return false;

		return true;
		}

	@Override
	public int hashCode()
		{
		int result = m_first != null ? m_first.hashCode() : 0;
		result = 31 * result + (m_second != null ? m_second.hashCode() : 0);
		result = 31 * result + (m_third != null ? m_third.hashCode() : 0);
		return result;
		}
	}
