package com.seaice.csar.seaiceprototype;

/**
 * Created by Kevin Avendaño on 23-04-16.
 */
import java.util.ArrayList;

public class ProtocolParser {
    public ProtocolParser()
    {
        //TODO algo aqui?
    }

    public ArrayList<Information> parse(String sms)
    {
        String[] distinctIds = sms.split("~");
        ArrayList<Information> list = new ArrayList<>();

        int state;
        int id = 0;
        int type = 0;
        for(String strId :distinctIds)
        {
            String[] content = strId.split("\\^");
            state = 0;
            /*
             * 0 es id
             * 1 es type
             * 2 es info
             */
            for(String element : content)
            {
                //parse each

                switch(state)
                {
                    case 0:
                        id = Integer.parseInt(element);
                        break;
                    case 1:
                        type = Integer.parseInt(element);
                        break;
                    case 2:
                        //Nuevo objeto
                        Information inf = new Information();
                        inf.type = type;
                        inf.id = id;

                        switch(type)
                        {
                            //Texto
                            case 0:
                                inf.text = element;
                                break;
                            //Viento
                            case 1:
                                String[] numDir = element.split(",");
                                inf.number = Float.parseFloat(numDir[0]);
                                inf.direction = Float.parseFloat(numDir[1]);
                                break;
                            //Temperatura
                            case 2:
                                inf.number = Float.parseFloat(element);
                                break;
                            //Hielo
                            case 3:
                                inf.number = Float.parseFloat(element);
                                break;
                        }

                        //Añadir a la lista
                        list.add(inf);
                        break;
                }

                //Cambio de estado
                //Si es 0, siguiente se espera tipo
                //Si es 1, siguiente se espera info
                //Si es 2, siguiente se espera tipo
                state = (state == 0) ? 1 : ((state == 1) ? 2 : 1);
            }
        }
        return list;
    }

    public String codify(ArrayList<Information> data)
    {

        int id = data.get(0).id;
        String res = id + "";

        for(Information element : data)
        {
            if(id != element.id)
            {
                id = element.id;
                res += "~";
                res += id;
            }

            res +="^"+element.type;
            switch(element.type)
            {
                case 0:
                    res +="^"+element.text;
                    break;
                case 1:
                    res +="^"+element.number+","+element.direction;
                    break;
                case 2:
                    res +="^"+element.number;
                    break;
                case 3:
                    res +="^"+element.number;
                    break;
            }
        }
        return res;
    }

    public class Information
    {
        int id;
        int type;
        float number;
        float direction;
        String text;

        public Information()
        {
            //Nada :V
        }

        @Override
        public String toString()
        {
            String info = "";
            switch(type)
            {
                case 0:
                    info = "Id:" + id + " Type:" + type + ": " + text;
                    break;
                case 1:
                    info = "Id:" + id + " Type:" + type + ": S:" + number + ", D:" + direction;
                    break;
                case 2:
                    info = "Id:" + id + " Type:" + type + ": T:" + number;
                    break;
                case 3:
                    info = "Id:" + id + " Type:" + type + ": W:" + number;
                    break;
            }
            return info;
        }
    }
}
