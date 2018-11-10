package tablesaw.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class TablesawClassLoader extends URLClassLoader
	{
	ClassLoader m_parentLoader;

	public TablesawClassLoader(URL[] urls, ClassLoader parent)
		{
		super(urls, parent);
		m_parentLoader = parent;
		}

	@Override
	public void addURL(URL url)
		{
		super.addURL(url);
		}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException
		{
		if (this.getClass().getName().equals(name))
			return this.getClass();
		
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);

		if (c == null)
			{
			// If still not found, then invoke findClass in order
			// to find the class.
			try
				{
				c = findClass(name);
				}
			catch (ClassNotFoundException e)
				{
				//pass to the parent to throw exception
				}
			}

		if (c == null)
			{
			c = m_parentLoader.loadClass(name);
			}
		if (resolve)
			{
			resolveClass(c);
			}
		return c;
		}

	@Override
	public URL getResource(String name)
		{
		URL url;

		url = findResource(name);

		if (url == null)
			url = m_parentLoader.getResource(name);

		return url;
		}


	@Override
	public Enumeration<URL> getResources(String name) throws IOException
		{
		Enumeration[] tmp = new Enumeration[2];
		tmp[0] = findResources(name);
		tmp[1] = m_parentLoader.getResources(name);

		return new CompoundEnumeration(tmp);
		}

	private class CompoundEnumeration<E> implements Enumeration<E>
		{
		private Enumeration[] enums;
		private int index = 0;

		public CompoundEnumeration(Enumeration[] var1)
			{
			this.enums = var1;
			}

		private boolean next()
			{
			while (this.index < this.enums.length)
				{
				if (this.enums[this.index] != null && this.enums[this.index].hasMoreElements())
					{
					return true;
					}

				++this.index;
				}

			return false;
			}

		public boolean hasMoreElements()
			{
			return this.next();
			}

		public E nextElement()
			{
			if (!this.next())
				{
				throw new NoSuchElementException();
				}
			else
				{
				return (E)this.enums[this.index].nextElement();
				}
			}
		}
	}
