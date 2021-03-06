/**
 * Copyright 2012 Lyncode
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Development @ Lyncode <development@lyncode.com>
 * @version 2.2.9
 */

package com.lyncode.xoai.serviceprovider.verbs;

import com.lyncode.xoai.serviceprovider.configuration.Configuration;
import com.lyncode.xoai.serviceprovider.iterators.MetadataFormatIterator;
import com.lyncode.xoai.serviceprovider.util.URLEncoder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Development @ Lyncode <development@lyncode.com>
 * @version 2.2.9
 */
public class ListMetadataFormats extends AbstractVerb
{
    private String proxyIp;
    private int proxyPort;
    private ExtraParameters parameters;

    public ListMetadataFormats(Configuration config, String baseUrl, String proxyIp, int proxyPort)
    {
        super(config, baseUrl);
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
        parameters = null;
    }
    public ListMetadataFormats(Configuration config, String baseUrl, String proxyIp, int proxyPort,
                               ExtraParameters extra)
    {
        super(config, baseUrl);
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
        parameters = extra;
    }

    public MetadataFormatIterator iterator()
    {
        return new MetadataFormatIterator(super.getConfiguration(), super.getBaseUrl(), this.proxyIp, this.proxyPort,
                parameters);
    }


    public class ExtraParameters {
        private String identifier;
        
        public ExtraParameters()
        {
            super();
        }
        
        
        
        public String getIdentifier()
        {
            return identifier;
        }



        public void setIdentifier(String identifier)
        {
            this.identifier = identifier;
        }



        public String toUrl () {
            List<String> string = new ArrayList<String>();
            if (identifier != null) string.add("set="+URLEncoder.encode(identifier));
            return StringUtils.join(string, URLEncoder.SEPARATOR);
        }
    }
}
