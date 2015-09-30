package com.ait.lienzo.client.core.shape.wires;

import java.util.HashMap;
import java.util.Map;

import com.ait.lienzo.client.core.Attribute;
import com.ait.lienzo.client.core.event.AttributesChangedEvent;
import com.ait.lienzo.client.core.event.AttributesChangedHandler;
import com.ait.lienzo.client.core.event.NodeDragEndEvent;
import com.ait.lienzo.client.core.event.NodeDragEndHandler;
import com.ait.lienzo.client.core.event.NodeDragMoveEvent;
import com.ait.lienzo.client.core.event.NodeDragMoveHandler;
import com.ait.lienzo.client.core.event.NodeDragStartEvent;
import com.ait.lienzo.client.core.event.NodeDragStartHandler;
import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.Direction;
import com.ait.lienzo.shared.core.types.DragMode;

public class MagnetManager implements IMagnetManager
{
    private Map<String, Magnets> magnetRegistry = new HashMap<String, Magnets>();

    public IMagnets createMagnets(Shape shape, Point2DArray points)
    {
        ControlHandleList list = new ControlHandleList(shape);
        BoundingBox box = shape.getBoundingBox();

        double left = box.getX();
        double right = left + box.getWidth();
        double top = box.getY();
        double bottom = top + box.getHeight();

        Magnets magnets = new Magnets(this, list, shape);

        for (Point2D p : points)
        {
            Magnet m = new Magnet(magnets, null, 0, p.getX(), p.getY(),
                                  getControlPrimitive(p.getX(), p.getY(), shape), true);
            Direction d = getDirection(p, left, right, top, bottom);
            m.setDirection(d);
            list.add(m);
        }



        magnetRegistry.put(shape.uuid(), magnets);

        return magnets;
    }

    public Direction getDirection(Point2D point, double left, double right, double top, double bottom)
    {
        double x = point.getX();
        double y = point.getY();

        double leftDist = Math.abs(x - left);
        double rightDist = Math.abs(x - right);

        double topDist = Math.abs(y - top);
        double bottomDist = Math.abs(y - bottom);

        boolean moreLeft = leftDist < rightDist;
        boolean moreTop = topDist < bottomDist;


        if ( moreLeft )
        {
            if ( moreTop)
            {
                if ( topDist <  leftDist  )
                {
                    return Direction.NORTH;
                }
                else if ( topDist >  leftDist  )
                {
                    return Direction.WEST;
                }
                else
                {
                    return Direction.NORTH_WEST;
                }
            }
            else
            {
                if ( bottomDist <  leftDist  )
                {
                    return Direction.SOUTH;
                }
                else if ( bottomDist >  leftDist  )
                {
                    return Direction.WEST;
                }
                else
                {
                    return Direction.SOUTH_WEST;
                }
            }
        }
        else
        {
            if ( moreTop)
            {
                if ( topDist <  rightDist  )
                {
                    return Direction.NORTH;
                }
                else if ( topDist >  rightDist  )
                {
                    return Direction.EAST;
                }
                else
                {
                    return Direction.NORTH_EAST;
                }
            }
            else
            {
                if ( bottomDist <  rightDist  )
                {
                    return Direction.SOUTH;
                }
                else if ( bottomDist >  rightDist  )
                {
                    return Direction.EAST;
                }
                else
                {
                    return Direction.SOUTH_EAST;
                }
            }
        }
    }

    private static Circle getControlPrimitive(double x, double y, Shape shape)
    {
        return new Circle(5).setFillColor(ColorName.RED).setFillAlpha(0.4).setX(x + shape.getX() ).setY(y + shape.getY()).setDraggable(true).setDragMode(DragMode.SAME_LAYER).setStrokeColor(ColorName.BLACK).setStrokeWidth(2);
    }

    public static class Magnets implements IMagnets, AttributesChangedHandler, NodeDragStartHandler, NodeDragMoveHandler, NodeDragEndHandler
    {
        private IControlHandleList m_handleList;

        private MagnetManager      m_magnetManager;

        private Shape              m_shape;

        private boolean            m_isDragging;

        public Magnets(MagnetManager magnetManager, IControlHandleList handleList, Shape shape)
        {
            m_handleList = handleList;
            m_magnetManager = magnetManager;
            m_shape = shape;
            shape.addAttributesChangedHandler(Attribute.X, this);
            shape.addAttributesChangedHandler(Attribute.Y, this);
            shape.addNodeDragMoveHandler(this);
        }

        public void onAttributesChanged(AttributesChangedEvent event)
        {
            if (!m_isDragging && event.any(Attribute.X, Attribute.Y))
            {
                shapeMoved();
            }
        }

        @Override public void onNodeDragStart(NodeDragStartEvent event)
        {
            m_isDragging = true;
        }

        @Override public void onNodeDragEnd(NodeDragEndEvent event)
        {
            m_isDragging = false;
        }

        @Override public void onNodeDragMove(NodeDragMoveEvent event)
        {
            shapeMoved();
        }

        public void shapeMoved()
        {
            double x = m_shape.getX();
            double y = m_shape.getY();
            for (int i = 0; i < m_handleList.size(); i++)
            {
                Magnet m = (Magnet) m_handleList.getHandle(i);
                m.shapeMoved(x, y);
            }
            m_handleList.getLayer().batch();
        }

        public void show()
        {
            m_handleList.show();
        }

        public void hide()
        {
            m_handleList.hide();
        }

        public void destroy()
        {
            m_handleList.destroy();
            m_magnetManager.magnetRegistry.remove(m_shape.uuid());
        }

        public void destroy(Magnet magnet)
        {

            m_handleList.remove(magnet);
        }

        public IControlHandleList getMagnets()
        {
            return m_handleList;
        }

        public Magnet getMagnet(int index)
        {
            return (Magnet) m_handleList.getHandle(index);
        }
    }
}