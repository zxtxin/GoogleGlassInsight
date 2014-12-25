precision mediump float;
uniform sampler2D u_Texture2;
uniform int Ni;
varying vec2 texCoords;
const lowp float texWidth = 1.0 / 256.0;
void main()
{
    vec2 current_point = texCoords;
    vec2 left_point = vec2(texCoords.s - texWidth * float(Ni), texCoords.t);
    if(left_point.s<0.0)
        gl_FragColor = 0.5*texture2D( u_Texture2, current_point) ;
    else
        gl_FragColor = 0.5*texture2D( u_Texture2, current_point) + 0.5*texture2D( u_Texture2, left_point );
}