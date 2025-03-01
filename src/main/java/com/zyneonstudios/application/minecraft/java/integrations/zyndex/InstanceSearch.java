package com.zyneonstudios.application.minecraft.java.integrations.zyndex;

import com.zyneonstudios.nexus.index.ReadableZyndex;
import com.zyneonstudios.nexus.instance.ReadableZynstance;

import java.util.ArrayList;

public class InstanceSearch {

    private final ArrayList<ReadableZynstance> instances;
    private ArrayList<ReadableZynstance> cachedResults = null;
    private String cachedSearchTerm = null;
    private final boolean officialSource;

    public InstanceSearch(String zyndexUrl) {
        instances = new ReadableZyndex(zyndexUrl).getInstances();
        officialSource = isOfficial(zyndexUrl);
    }

    private boolean isOfficial(String url) {
        ArrayList<String> officialUrls = new ArrayList<>();
        officialUrls.add("https://raw.githubusercontent.com/zyneonstudios/nexus-nex/main/zyndex/index.json");
        officialUrls.add("https://zyneonstudios.github.io/nexus-nex/main/zyndex/index.json");
        officialUrls.add("https://raw.githubusercontent.com/zyneonstudios/nexus-nex/main/zyndex/index");
        officialUrls.add("https://zyneonstudios.github.io/nexus-nex/main/zyndex/index");
        officialUrls.add("https://raw.githubusercontent.com/zyneonstudios/nexus-nex/main/zyndex/");
        officialUrls.add("https://zyneonstudios.github.io/nexus-nex/main/zyndex/");
        officialUrls.add("https://raw.githubusercontent.com/zyneonstudios/nexus-nex/main/zyndex");
        officialUrls.add("https://zyneonstudios.github.io/nexus-nex/main/zyndex");
        return officialUrls.contains(url.toLowerCase());
    }

    @SuppressWarnings("all")
    public ArrayList<ReadableZynstance> search(String searchTerm) {
        if(!searchTerm.replace(" ","").isEmpty()) {
            cachedSearchTerm = searchTerm;
        }

        ArrayList<ReadableZynstance> results = new ArrayList<>();
        String[] searchTerms = searchTerm.toLowerCase().replace(" ",",").replace(",,",",").split(",");

        if(!instances.isEmpty()) {
            for(ReadableZynstance instance : instances) {
                boolean idMatching = false;
                for(String s:searchTerms) {
                    if (instance.getId().equalsIgnoreCase(s)) {
                        idMatching = true;
                        break;
                    }
                }

                if(instance.getName().toLowerCase().contains(searchTerm.toLowerCase())||idMatching) {
                    if(!instance.isHidden()||idMatching) {
                        results.add(instance);
                    }
                }
            };
        }

        cachedResults = results;
        return cachedResults;
    }

    public ArrayList<ReadableZynstance> getCachedResults() {
        return cachedResults;
    }

    public String getCachedSearchTerm() {
        return cachedSearchTerm;
    }
}