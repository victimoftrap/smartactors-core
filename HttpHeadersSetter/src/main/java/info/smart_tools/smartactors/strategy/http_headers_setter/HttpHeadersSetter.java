package info.smart_tools.smartactors.strategy.http_headers_setter;

import info.smart_tools.smartactors.core.ifield.IField;
import info.smart_tools.smartactors.core.ifield_name.IFieldName;
import info.smart_tools.smartactors.core.iheaders_extractor.IHeadersSetter;
import info.smart_tools.smartactors.core.iheaders_extractor.exceptions.HeadersSetterException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.List;

/**
 * Headers setter for {@link FullHttpResponse}
 * This implementation extract headers from context of the environment and set them into response
 * Headers should presents as {@link List<IObject>}
 * <pre>
 *     "headers": [
 *         {
 *             "name": "nameOfTheCookie",
 *             "value": "valueOfTheCookie"
 *         }
 *     ]
 * </pre>
 */

public class HttpHeadersSetter implements IHeadersSetter {
    @Override
    public void set(final Object response, final IObject environment) throws HeadersSetterException {
        FullHttpResponse httpResponse = (FullHttpResponse) response;
        IField contextField;
        IField headersField;
        IFieldName headerName;
        IFieldName headerValue;
        try {
            contextField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "context");
            headersField = IOC.resolve(Keys.getOrAdd(IField.class.getCanonicalName()), "cookies");
            headerName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "name");
            headerValue = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "value");
        } catch (ResolutionException e) {
            throw new HeadersSetterException("Failed to resolve fieldName", e);
        }
        IObject context = null;
        List<IObject> headers = null;
        try {
            context = contextField.in(environment, IObject.class);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new HeadersSetterException("Failed to get context from environment", e);
        }
        try {
            headers = headersField.in(context, List.class);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new HeadersSetterException("Failed to get cookies from context", e);
        }
        for (IObject header : headers) {
            try {
                httpResponse.headers().set(header.getValue(headerName).toString(),
                        header.getValue(headerValue).toString());
            } catch (ReadValueException | InvalidArgumentException e) {
                throw new HeadersSetterException("Failed to resolve header", e);
            }
        }
        httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, httpResponse.content().readableBytes());
        httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        httpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
    }
}
