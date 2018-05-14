package com.assigment2.app;

// import the rest service you created!
import com.assigment2.rest.SocialMediaService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SocialMediaApplication extends Application
{

    private static final Set< Object > singletons = new HashSet< Object >();

    public SocialMediaApplication() {
        try {
            singletons.add(new SocialMediaService());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set< Class< ? > > getClasses()
    {
        HashSet< Class< ? > > set = new HashSet< Class< ? > >();
        return set;
    }

    @Override
    public Set< Object > getSingletons()
    {
        return singletons;
    }

}
