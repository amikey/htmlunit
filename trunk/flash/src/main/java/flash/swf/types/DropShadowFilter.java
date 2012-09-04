////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2005-2006 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

package flash.swf.types;

/**
 * A value object for drop shadow filter data.
 *
 * @author Roger Gonzalez
 */
public class DropShadowFilter extends Filter
{
    public final static int ID = 0;
    public int getID() { return ID; }
    public int color;
    public int blurX;
    public int blurY;
    public int angle;   // really 8.8 fixedpoint, but we don't care yet
    public int distance;
    public int strength;
    public int flags;
}
