package rmi;

import handler.Pair;
import handler.RmiRequest;
import handler.client.RmiResponse;
import io.netty.channel.ChannelHandlerContext;
import proxy.WzCallback;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/6/16.
 */
public abstract class RemoteService {
    protected Map<String, Object> registerObject;

    RmiServer.CallbackProxy callbackProxy = new CallbackProxy();

    protected RemoteService() {
        registerObject = new HashMap<>();
    }

    public Object executeServerMethod(ChannelHandlerContext ctx, RmiRequest request) {
        int paramSize = request.getParamsMap().size();
        Class<?>[] classType = new Class<?>[paramSize];
        Object[] value = new Object[paramSize];
        int index = 0;
        for (Pair<String, Object> e : request.getParamsMap()) {
            classType[index] = getClassType(e.getK());
            if (classType[index].getSimpleName().equals("WzCallback")) {
                value[index] = getInterface(WzCallback.class);
            } else {
                value[index] = e.getV();
            }
            index++;
        }
        try {
            Object result = executeServerCommand(request.getInterfaceName(), request.getMethodName(), classType, value);
            if (result == null) {
                ctx.writeAndFlush(new RmiResponse(result));
            } else {
                if (!result.equals(java.lang.Void.class)) {
                    ctx.writeAndFlush(new RmiResponse(result));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract void response(RmiResponse response);

    public <T> T getInterface(Class<T> classType) {
        return (T) Proxy.newProxyInstance(classType.getClassLoader(), new Class[]{classType}, callbackProxy);
    }

    Object executeServerCommand(String className, String methodName, Class<?>[] args, Object[] values) throws Exception {
        Object obj = registerObject.get(className);
        if (obj == null) {
            return 0;
        }
        int argsLength = args.length;
        switch (argsLength) {
            case 0:
                return executeArgsMethod0(obj, methodName, args, values);
            case 1:
                return executeArgsMethod1(obj, methodName, args, values);
            case 2:
                return executeArgsMethod2(obj, methodName, args, values);
            case 3:
                return executeArgsMethod3(obj, methodName, args, values);
            case 4:
                return executeArgsMethod4(obj, methodName, args, values);
            case 5:
                return executeArgsMethod5(obj, methodName, args, values);
            case 6:
                return executeArgsMethod6(obj, methodName, args, values);
            case 7:
                return executeArgsMethod7(obj, methodName, args, values);
            case 8:
                return executeArgsMethod8(obj, methodName, args, values);
            case 9:
                return executeArgsMethod9(obj, methodName, args, values);
            case 10:
                return executeArgsMethod10(obj, methodName, args, values);
            default:
                throw new RuntimeException("un support num of args exception");
        }
    }

    private Class<?> getClassType(String simpleName) {
        switch (simpleName) {
            case "Integer":
                return int.class;
            case "Short":
                return short.class;
            case "String":
                return String.class;
            case "":
                return WzCallback.class;
            default:
                throw new RuntimeException("un support type exception");
        }
    }

    public void registerObject(Class<?> classType) {
        Class<?>[] interfaces = classType.getInterfaces();
        if (interfaces == null) {
            throw new RuntimeException("register object don't implements any interface");
        }
        for (Class<?> face : interfaces) {
            try {
                registerObject0(face, classType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Object executeArgsMethod0(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        return method.invoke(obj);
    }

    private void registerObject0(Class<?> faceType, Class<?> classType) throws Exception {
        Object value = registerObject.get(faceType.getSimpleName());
        if (value == null) {
            value = classType.newInstance();
        }
        registerObject.put(faceType.getSimpleName(), value);
    }

    Object executeArgsMethod1(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName, typeArray[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        return method.invoke(obj, objectArray[0]);
    }


    Object executeArgsMethod2(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName, typeArray[0], typeArray[1]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        return method.invoke(obj, objectArray[0], objectArray[1]);
    }

    Object executeArgsMethod3(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName, typeArray[0], typeArray[1], typeArray[2]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        if (method.getReturnType().getSimpleName().equals("void")) {
            method.invoke(obj, objectArray[0], objectArray[1], objectArray[2]);
            return Void.class;
        } else {
            return method.invoke(obj, objectArray[0], objectArray[1], objectArray[2]);
        }
    }

    Object executeArgsMethod4(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName, typeArray[0], typeArray[1], typeArray[2], typeArray[3]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        return method.invoke(obj, objectArray[0], objectArray[1], objectArray[2], objectArray[3]);
    }

    Object executeArgsMethod5(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName, typeArray[0], typeArray[1], typeArray[2], typeArray[3], typeArray[4]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        return method.invoke(obj, objectArray[0], objectArray[1], objectArray[2], objectArray[3], objectArray[4]);
    }

    Object executeArgsMethod6(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName, typeArray[0], typeArray[1], typeArray[2], typeArray[3], typeArray[4], typeArray[5]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        return method.invoke(obj, objectArray[0], objectArray[1], objectArray[2], objectArray[3], objectArray[4], objectArray[5]);
    }

    Object executeArgsMethod7(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName, typeArray[0], typeArray[1], typeArray[2], typeArray[3], typeArray[4], typeArray[5]
                    , typeArray[6]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        return method.invoke(obj, objectArray[0], objectArray[1], objectArray[2], objectArray[3], objectArray[4], objectArray[5]
                , objectArray[6]);
    }

    Object executeArgsMethod8(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName, typeArray[0], typeArray[1], typeArray[2], typeArray[3], typeArray[4], typeArray[5]
                    , typeArray[6], typeArray[7]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        return method.invoke(obj, objectArray[0], objectArray[1], objectArray[2], objectArray[3], objectArray[4], objectArray[5]
                , objectArray[6], objectArray[7]);
    }

    Object executeArgsMethod9(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName, typeArray[0], typeArray[1], typeArray[2], typeArray[3], typeArray[4], typeArray[5]
                    , typeArray[6], typeArray[7], typeArray[8]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        return method.invoke(obj, objectArray[0], objectArray[1], objectArray[2], objectArray[3], objectArray[4], objectArray[5]
                , objectArray[6], objectArray[7], objectArray[8]);
    }

    Object executeArgsMethod10(Object obj, String methodName, Class<?>[] typeArray, Object[] objectArray) throws Exception {
        Method method = null;
        try {
            method = obj.getClass().getMethod(methodName, typeArray[0], typeArray[1], typeArray[2], typeArray[3], typeArray[4], typeArray[5]
                    , typeArray[6], typeArray[7], typeArray[8], typeArray[9]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            return 0;
        }
        return method.invoke(obj, objectArray[0], objectArray[1], objectArray[2], objectArray[3], objectArray[4], objectArray[5]
                , objectArray[6], objectArray[7], objectArray[8], objectArray[9]);
    }


    class CallbackProxy implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //don't do any thing but send call to client
            return null;
        }
    }
}
