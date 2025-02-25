package cn.iocoder.yudao.framework.common.util.collection;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.util.ArrayUtil;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * Array 工具类
 *
 * @author 芋道源码
 */
public class ArrayUtils {

    /**
     * 将 object 和 newElements 合并成一个数组
     *
     * @param object 对象
     * @param newElements 数组
     * @param <T> 泛型
     * @return 结果数组
     */
    @SafeVarargs
    public static <T> Consumer<T>[] append(Consumer<T> object, Consumer<T>... newElements) {
        if (object == null) {
            return newElements;
        }
        Consumer<T>[] result = ArrayUtil.newArray(Consumer.class, 1 + newElements.length);
        result[0] = object;
        System.arraycopy(newElements, 0, result, 1, newElements.length);
        return result;
    }

    public static <T, V> V[] toArray(Collection<T> from, Function<T, V> mapper) {
        return toArray(convertList(from, mapper));
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> from) {
        if (CollectionUtil.isEmpty(from)) {
            return (T[]) (new Object[0]);
        }
        return ArrayUtil.toArray(from, (Class<T>) IterUtil.getElementType(from.iterator()));
    }

    public static <T> T get(T[] array, int index) {
        if (null == array || index >= array.length) {
            return null;
        }
        return array[index];
    }



    /**
     * 转成非只读 List
     * @param <T> 类型
     * @param els 元素
     * @return 非只读 List
     * */
    public static <T> Set<T> asSet(T... els)
    {
        return new HashSet<T>(Arrays.asList(els));
    }

    /**
     * 转成非只读 List
     * @param <T> 类型
     * @param els 元素
     * @return 非只读 List
     * */
    public static <T> List<T> asList(T... els)
    {
        return new ArrayList<T>(Arrays.asList(els));
    }

    /**
     * 把多个数组按顺序合并成一个数组
     * @param <T>  数组类型
     * @param arrays  将要合并的数组
     * @return 合并后的数组
     * */
    public static <T> T[] merge(T[]... arrays)
    {
        Class type=null;
        int total=0;
        for(T[] array : arrays) {
            type=array.getClass();
            type=type.getComponentType();
            if(array==null) continue;
            total+=array.length;
        }

        if(type==null) {
            throw new RuntimeException("无法识别数组类型");
        }

        T[] newArr=createArray((Class<T>)type, total);

        int i=0;
        for(Object[] array : arrays) {
            System.arraycopy(array,0, newArr, i, array.length);
            i+=array.length;
        }
        return newArr;

    }


    /**
     * 把多个数组按顺序合并成一个数组
     * @param <T>  数组类型
     * @param arrays  将要合并的数组
     * @return 合并后的数组
     * */
    public static <T> T[] mergex(T[]... arrays)
    {

        List<T> list=new ArrayList<T>();
        for(T[] array : arrays) {
            list.addAll(Arrays.asList(array));
        }
        return toArray(list);
    }




    /**
     * 截取子数组
     * @param <T>  数组类型
     * @param source 源数组
     * @param startIndex 开始位置
     * @param endIndex  结束位置
     * @return  返回子数组
     * */
    @SuppressWarnings("unchecked")
    public static <T> T[] subArray(T[] source,int startIndex,int endIndex)
    {

        if(source==null || source.length==0) return source;
        if(endIndex>source.length-1) {
            endIndex=source.length-1;
        }
        if(startIndex<0) {
            startIndex=0;
        }
        //确定类型
        Class type=source.getClass().getComponentType();

        if(startIndex>endIndex) {
            throw new IllegalArgumentException("startIndex不允许超过endIndex");
        }

        T[] arr=(T[])createArray(type, endIndex-startIndex+1);
        for (int i = startIndex; i <= endIndex; i++) {
            arr[i - startIndex]=source[i];
        }
        return arr;
    }

    /**
     * 把数组拼接成字符串，默认用逗号隔开
     *
     * @param array 输入数组
     * @return 返回字符串
     */
    public static String join(Object[] array) {
        return join(array,",","");
    }

    /**
     * 把数组拼接成字符串
     *
     * @param sep   分隔符
     * @param array 输入数组
     * @return 返回字符串
     */
    public static String join(Object[] array,String sep,String quote)
    {
        if(array==null) return null;
        if(quote==null) quote="";
        StringBuilder buf=new StringBuilder();
        for (int i = 0; i < array.length; i++) {

            if(i<array.length-1) {
                buf.append(quote+array[i]+quote+sep);
            } else {
                buf.append(quote+array[i]+quote);
            }
        }
        return buf.toString();
    }

    /**
     * 把元素合并到数组的最前面，相当于List.addAt(0)
     * @param <T>  数组类型
     * @param array	数组
     * @param els 添加的元素
     * @return  返回添加好合并后的数组
     * */
    public static <T> T[] unshift(T[] array,T... els)
    {
        return merge(els,array);
    }

    /**
     * 把元素合并到数组的最后面，相当于List.add()
     * @param <T>  数组类型
     * @param array	数组
     * @param els 添加的元素
     * @return  返回添加好合并后的数组
     * */
    public static <T> T[] append(T[] array,T... els)
    {
        return merge(array,els);
    }


    /**
     * 把数组合并到list的最后面，相当于List.add()
     * @param <T> 类型
     * @param list	列表
     * @param arrays 添加的数组
     * @return  返回添加好合并后的数组
     * */
    public static <T> List<T> addToList(List<T> list,T[]... arrays)
    {
        for (T[] objects : arrays) {
            list.addAll(Arrays.asList(objects));
        }
        return list;
    }

    /**
     * 把数组合并到list
     *
     * @param <T> 类型
     * @param arrays 添加的数组
     * @return  返回添加好合并后的数组
     * */
    public static <T> List<T> mergeToList(T[]... arrays)
    {
        List<T> list=new ArrayList<T>();
        addToList(list, arrays);
        return list;

    }

    /**
     * 数组是否包含某个元素
     * @param <T> 类型
     * @param array 数组
     * @param e 元素
     * @return 是否包含
     * */
    public static <T> boolean contains(T[] array,T e)
    {
        return indexOf(array,e,0) != -1;
    }

    /**
     * 数组是否包含某个元素
     * @param <T> 类型
     * @param array 数组
     * @param e 元素
     * @param beginIndex 开始位置
     * @return 匹配的第一个位置
     * */
    public static <T> int indexOf(T[] array,T e,int beginIndex)
    {
        if(beginIndex<0) beginIndex=0;
        T o = null;
        int index=-1;
        for (int i = beginIndex ; i < array.length; i++) {
            o=array[i];
            if(o.equals(e)) {
                index = i;
                break;
            }
        }
        return index;
    }


    /**
     * 数组是否包含某个元素
     * @param array 数组
     * @param e 元素
     * @param ignorCase 是否忽略大小写
     * @return 是否包含
     * */
    public static boolean contains(String[] array,String e,boolean ignorCase)
    {
        return indexOf(array,e,ignorCase) != -1;
    }

    /**
     * 定位元素位置
     * @param array 数组
     * @param e 元素
     * @param ignorCase 是否忽略大小写
     * @return 元素位置
     * */
    public static int indexOf(String[] array,String e,boolean ignorCase)
    {
        return indexOf(array,e,0,ignorCase);
    }

    /**
     * 定位元素位置
     * @param array 数组
     * @param e 元素
     * @param beginIndex  开始位置
     * @param ignorCase 是否忽略大小写
     * @return 元素位置
     * */
    public static int indexOf(String[] array,String e,int beginIndex,boolean ignorCase)
    {
        if(beginIndex<0) beginIndex=0;
        String o = null;
        int index=-1;
        for (int i = beginIndex ; i < array.length; i++) {
            o=array[i];
            if(ignorCase)
            {
                if(o.equalsIgnoreCase(e)) {
                    index = i;
                    break;
                }
            }
            else
            {
                if(o.equals(e)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    /**
     * 移除值为 null 的元素
     * @param arr 数组
     * @return 没有 null 值元素的数组
     * */
    public static Object[] removeNulls(Object[] arr)
    {
        ArrayList<Object> tmp=new ArrayList<Object>();
        for (int i = 0; i < arr.length; i++) {
            if(arr[i]==null) {
                continue;
            }
            tmp.add(arr[i]);
        }
        return tmp.toArray();
    }

    /**
     * 转小写
     * @param arr 数组
     * @return 元素小写的数组
     * */
    public static String[] toLowerCase(String [] arr)
    {
        for (int i = 0; i < arr.length; i++) {
            if(arr[i]==null) {
                continue;
            }
            arr[i]=arr[i].toLowerCase();
        }
        return arr;
    }

    /**
     * 转大写
     * @param arr 数组
     * @return 元素大写的数组
     * */
    public static String[] toUpperCase(String [] arr)
    {
        for (int i = 0; i < arr.length; i++) {
            if(arr[i]==null) {
                continue;
            }
            arr[i]=arr[i].toUpperCase();
        }
        return arr;
    }




    /**
     * List 转 Array
     * @param <T> 类型
     * @param list list对象
     * @return  数组
     * */
    public static <T> T[] toArray(List<T> list)
    {
        //确定类型
        Object sample = null;
        for (T t : list) {
            if (t != null) {
                sample = t;
                break;
            }
        }
        if(sample==null) throw new IllegalArgumentException("无法识别列表类型");
        Class<T> type=(Class<T>)sample.getClass();
        T[] arr=createArray(type, list.size());
        return list.toArray(arr);
    }

    /**
     * 动态创建数组
     * @param <T> 类型
     * @param type 数组类型
     * @param size  数组大小
     * @return 数组
     * */
    @SuppressWarnings("unchecked")
    public static <T> T[] createArray(Class<T> type,int size)
    {
        return (T[]) Array.newInstance(type, size);
    }


    public static <T> List<T> toList(T[] arr) {
        return Arrays.asList(arr);
    }

    public static  <T> Set<T> toSet(T[] arr) {
        Set<T> set=new HashSet<>(arr.length);
        for (T t : arr) {
            set.add(t);
        }
        return set;
    }


}
