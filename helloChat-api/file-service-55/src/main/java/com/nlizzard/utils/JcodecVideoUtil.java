package com.nlizzard.utils;

import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.ByteBufferSeekableByteChannel;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * JCodec 视频帧截取工具类
 *
 * 作用：
 * 1. 支持从本地视频路径中截取视频帧；
 * 2. 支持从本地 File 视频文件中截取视频帧；
 * 3. 支持从 MultipartFile 上传视频中截取视频帧；
 * 4. 截取结果不保存到本地，直接返回 byte[] 或 InputStream；
 * 5. 适合配合 MinIO、OSS、S3 等对象存储上传使用。
 */
@Slf4j
public class JcodecVideoUtil {

    /**
     * 截取出来的图片格式。
     *
     * 目前固定为 jpg。
     */
    private static final String FILE_EXT = "jpg";

    /**
     * 截取第几帧。
     *
     * 注意：
     * JCodec 的帧下标一般从 0 开始。
     * 0 表示第一帧。
     * 5 表示第六帧附近，或者可理解为跳过前面几帧后再截取。
     *
     * 如果你要获取视频第一帧，应该设置为 0。
     */
    private static final int THUMB_FRAME = 0;

    /**
     * 根据本地视频路径截取视频帧，并返回图片字节数组。
     *
     * @param videoFilePath 本地视频文件路径，例如：D:/videos/test.mp4
     * @return 截取出来的视频帧图片字节数组，格式为 jpg
     * @throws IllegalArgumentException 当 videoFilePath 为空时抛出
     * @throws RuntimeException         当视频帧截取失败时抛出
     */
    public static byte[] fetchFrameBytes(String videoFilePath) {
        if (videoFilePath == null || videoFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("视频文件路径不能为空");
        }

        File videoFile = new File(videoFilePath);
        return fetchFrameBytes(videoFile);
    }

    /**
     * 根据本地视频 File 对象截取视频帧，并返回图片字节数组。
     *
     * @param videoFile 本地视频文件对象，例如 new File("D:/videos/test.mp4")
     * @return 截取出来的视频帧图片字节数组，格式为 jpg
     * @throws IllegalArgumentException 当 videoFile 为空、不存在或不是文件时抛出
     * @throws RuntimeException         当视频帧截取失败时抛出
     */
    public static byte[] fetchFrameBytes(File videoFile) {
        if (videoFile == null || !videoFile.exists() || !videoFile.isFile()) {
            throw new IllegalArgumentException("视频文件不存在");
        }

        try (SeekableByteChannel channel = NIOUtils.readableChannel(videoFile)) {
            return grabFrameBytes(channel);
        } catch (IOException | JCodecException e) {
            log.error("获取视频帧异常：", e);
            throw new RuntimeException("获取视频帧失败", e);
        }
    }

    /**
     * 根据 MultipartFile 上传视频截取视频帧，并返回图片字节数组。
     *
     * 这个方法适合处理前端上传的视频文件。
     *
     * 注意：
     * 1. 不会把上传视频保存到本地；
     * 2. 不会把截图图片保存到本地；
     * 3. 会通过 videoFile.getBytes() 把整个视频读入内存；
     * 4. 如果视频很大，建议限制上传文件大小。
     *
     * @param videoFile 前端上传的视频文件，Spring MVC 中常见类型为 MultipartFile
     * @return 截取出来的视频帧图片字节数组，格式为 jpg
     * @throws IllegalArgumentException 当 videoFile 为空或文件内容为空时抛出
     * @throws RuntimeException         当视频帧截取失败时抛出
     */
    public static byte[] fetchFrameBytes(MultipartFile videoFile) {
        if (videoFile == null || videoFile.isEmpty()) {
            throw new IllegalArgumentException("视频文件不能为空");
        }

        try {
            byte[] videoBytes = videoFile.getBytes();

            if (videoBytes.length == 0) {
                throw new IllegalArgumentException("视频文件内容为空");
            }

            try (SeekableByteChannel channel =
                         new ByteBufferSeekableByteChannel(
                                 ByteBuffer.wrap(videoBytes),
                                 videoBytes.length
                         )) {
                return grabFrameBytes(channel);
            }

        } catch (IOException | JCodecException e) {
            log.error("获取视频帧异常：", e);
            throw new RuntimeException("获取视频帧失败", e);
        }
    }

    /**
     * 根据本地视频路径截取视频帧，并返回 InputStream。
     *
     * 适合直接上传到 MinIO、OSS 等对象存储。
     *
     * @param videoFilePath 本地视频文件路径
     * @return 截取出来的视频帧图片输入流，格式为 jpg
     */
    public static InputStream fetchFrameInputStream(String videoFilePath) {
        byte[] bytes = fetchFrameBytes(videoFilePath);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 根据本地视频 File 对象截取视频帧，并返回 InputStream。
     *
     * @param videoFile 本地视频文件对象
     * @return 截取出来的视频帧图片输入流，格式为 jpg
     */
    public static InputStream fetchFrameInputStream(File videoFile) {
        byte[] bytes = fetchFrameBytes(videoFile);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 根据 MultipartFile 上传视频截取视频帧，并返回 InputStream。
     * @param videoFile 前端上传的视频文件
     * @return 截取出来的视频帧图片输入流，格式为 jpg
     */
    public static InputStream fetchFrameInputStream(MultipartFile videoFile) {
        byte[] bytes = fetchFrameBytes(videoFile);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 真正执行视频帧截取的方法。
     *
     * 这是内部私有方法，不建议外部直接调用。
     *
     * 处理流程：
     * 1. 根据 SeekableByteChannel 创建 FrameGrab；
     * 2. 定位到指定帧；
     * 3. 获取该帧 Picture；
     * 4. 转换为 BufferedImage；
     * 5. 写入 ByteArrayOutputStream；
     * 6. 返回 jpg 图片字节数组。
     *
     * @param channel 视频文件对应的可随机访问字节通道
     * @return 截取出来的视频帧图片字节数组，格式为 jpg
     * @throws IOException     图片写入或通道读取异常
     * @throws JCodecException JCodec 解码异常
     */
    private static byte[] grabFrameBytes(SeekableByteChannel channel)
            throws IOException, JCodecException {

        FrameGrab frameGrab = FrameGrab.createFrameGrab(channel);

        Picture picture = frameGrab
                .seekToFramePrecise(THUMB_FRAME)
                .getNativeFrame();

        if (picture == null) {
            throw new RuntimeException("获取视频帧失败：视频帧为空");
        }

        BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        boolean success = ImageIO.write(bufferedImage, FILE_EXT, outputStream);
        if (!success) {
            throw new RuntimeException("图片写入失败：未找到 jpg 写入器");
        }

        byte[] imageBytes = outputStream.toByteArray();
        if (imageBytes.length == 0) {
            throw new RuntimeException("图片生成失败：字节内容为空");
        }

        return imageBytes;
    }
}