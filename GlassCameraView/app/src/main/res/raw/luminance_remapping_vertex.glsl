attribute vec4 vPosition;
attribute vec2 inputTextureCoordinate;
varying vec2 texCoords;
void main()
{

    texCoords = inputTextureCoordinate;
    gl_Position = vPosition;
}