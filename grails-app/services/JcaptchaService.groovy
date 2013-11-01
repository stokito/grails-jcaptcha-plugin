import com.octo.captcha.service.CaptchaService
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriter
import javax.imageio.metadata.IIOMetadata
import javax.imageio.plugins.jpeg.JPEGImageWriteParam
import javax.imageio.stream.ImageOutputStream
import javax.sound.sampled.AudioFileFormat.Type
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import java.awt.image.BufferedImage

import static javax.imageio.ImageWriteParam.MODE_EXPLICIT

/**
 * Provides access to the captchas as well as provides some util
 * type methods to convert captchas to usable data.
 *
 * @author LD <ld@ldaley.com>
 */
class JcaptchaService {
    /** Used to access the captchas defined as part of the app config. */
    GrailsApplication grailsApplication

    /**
     * Retrieves a captcha by name.
     *
     * @param captchaName The 'key' of the captcha defined in config.
     * @throws IllegalArgumentException If captchaName is null.
     * @throws IllegalStateException If there is no captcha by that name.
     * @returns The captcha service keyed by 'captchaName'
     */
    CaptchaService getCaptchaService(String captchaName) {
        if (captchaName == null) {
            throw IllegalArgumentException("'captchaName' cannot be null")
        }
        CaptchaService service = grailsApplication.config.jcaptchas[captchaName]
        if (!service) {
            throw new IllegalStateException("There is no jcaptcha defined with name '${captchaName}'")
        }
        return service
    }

    /**
     * Used to verify the response to a challenge.
     *
     * @param captchaName The key of the captcha
     * @param id The identifier used when retrieving the challenge (often session.id)
     * @param response What the user 'entered' to meet the challenge
     * @return True if the response meets the challenge
     * @see #getCaptchaService ()
     */
    boolean validateResponse(String captchaName, String id, String response) {
        def c = getCaptchaService(captchaName)
        return c.validateResponseForID(id, response)
    }

    /**
     * Utility routine to turn an image challenge into a JPEG stream.
     *
     * @param challenge The image data
     * @return A raw bunch of bytes which come together to be a JPEG.
     */
    byte[] challengeAsJpeg(BufferedImage challenge) {
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream()
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(jpegOutputStream)
        ImageWriter jpegEncoder = (ImageWriter) ImageIO.getImageWritersByFormatName('JPEG').next()

        JPEGImageWriteParam param = new JPEGImageWriteParam(null)
        param.setCompressionMode(MODE_EXPLICIT)
        param.setCompressionQuality(1.0F)

        jpegEncoder.setOutput(imageOutputStream)
        jpegEncoder.write((IIOMetadata) null, new IIOImage(challenge, null, null), param)
        return jpegOutputStream.toByteArray()
    }

    /**
     * Utility routine to turn a sound challenge into a WAV stream.
     *
     * @param challenge The sound data
     * @return A raw bunch of bytes which come together to be a WAV.
     */
    byte[] challengeAsWav(AudioInputStream challenge) {
        ByteArrayOutputStream soundOutputStream = new ByteArrayOutputStream()
        AudioSystem.write(challenge, Type.WAVE, soundOutputStream)
        soundOutputStream.flush()
        soundOutputStream.close()
        return soundOutputStream.toByteArray()
    }
}
