/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.shr.contenthandler.api;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception for content handlers to use in order to indicate that an error has occurred.
 * <br>
 * Allows for the capture of discrete detail items, for example if multiple validation errors occur when processing content.
 */
public class ContentHandlerException extends Exception {
    public enum DetailType { INFO, WARNING, ERROR }

    public static class Detail {
        private DetailType detailType;
        private String detail;

        public Detail(DetailType detailType, String detail) {
            this.detailType = detailType;
            this.detail = detail;
        }

        public DetailType getDetailType() {
            return detailType;
        }

        public void setDetailType(DetailType detailType) {
            this.detailType = detailType;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s", detailType, detail);
        }
    }

    private List<Detail> details = new ArrayList<Detail>();


    public ContentHandlerException() {
        super();
    }

    public ContentHandlerException(String message) {
        super(message);
    }

    public ContentHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentHandlerException(Throwable cause) {
        super(cause);
    }

    public ContentHandlerException(List<Detail> details) {
        this.details = details;
    }


    public List<Detail> getDetails() {
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    public void addDetail(Detail detail) {
        details.add(detail);
    }

    public boolean hasDetails() {
        return details !=null && !details.isEmpty();
    }


    @Override
    public String toString() {
        if (!hasDetails()) {
            return super.toString();
        }

        StringBuilder sb = new StringBuilder("[ContentHandlerException] Details of this exception are:\n");
        for (Detail detail : details) {
            sb.append(detail + "\n");
        }
        return sb.toString();
    }
}
