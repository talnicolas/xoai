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

import org.apache.log4j.Logger;

import com.lyncode.xoai.serviceprovider.iterators.RecordIterator;
import com.lyncode.xoai.serviceprovider.oaipmh.GenericParser;
import com.lyncode.xoai.serviceprovider.oaipmh.spec.RecordType;
import com.lyncode.xoai.serviceprovider.util.ProcessingQueue;


/**
 * @author Development @ Lyncode <development@lyncode.com>
 * @version 2.2.9
 */
public class ListRecords extends AbstractVerb
{
	
    private String metadataPrefix;
    private Parameters extra;
    private int interval;
    private String proxyIp;
    private int proxyPort;
    
    public ListRecords(String baseUrl, String metadataPrefix, int interval, String proxyIp, int proxyPort,
                       Logger log)
    {
        super(baseUrl, log);
        this.metadataPrefix = metadataPrefix;
        this.extra = null;
        this.interval = interval;
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
    }
    

    public ListRecords(String baseUrl, String metadataPrefix, Parameters extra, int interval, String proxyIp,
                       int proxyPort, Logger log)
    {
        super(baseUrl, log);
        this.metadataPrefix = metadataPrefix;
        this.extra = extra;
        this.interval = interval;
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
    }

    public ProcessingQueue<RecordType> harvest(GenericParser metadata)
    {
        return (new RecordIterator(this.interval, super.getBaseUrl(), metadataPrefix, extra, this.proxyIp,
                this.proxyPort, getLogger(), metadata)).harvest();
    }
    public ProcessingQueue<RecordType> harvest(GenericParser metadata, GenericParser about)
    {
        return (new RecordIterator(this.interval, super.getBaseUrl(), metadataPrefix, extra, this.proxyIp,
                this.proxyPort, getLogger(), metadata, about)).harvest();
    }
}
