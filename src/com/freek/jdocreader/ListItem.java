package com.freek.jdocreader;

import java.io.*;

public class ListItem implements Serializable
{
	private String name, description, path;
	
	public ListItem(String name, String description, String path)
	{
		this.name = name;
		this.description = description;
		this.path = path;
	}

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public String getPath()
	{
		return path;
	}
	
	
	@Override
	public String toString()
	{
		return name + " - " + description;	
	}
	
	@Override
	public boolean equals(Object other)
	{
		return this.path.equals(((ListItem)other).path);
	}
	
}