precision mediump float;
uniform sampler2D u_Texture2;
uniform sampler2D u_Texture0;
uniform sampler2D u_Texture1;
varying vec2 texCoords;
const mat3 coeffs = mat3(
  1.164,  1.164,  1.164,
  1.596, -0.813,  0.0,
  0.0  , -0.391,  2.018 );
const vec3 offset = vec3(0.0625, 0.5, 0.5);
const vec2 max_coord = vec2(1.0, 0.0);
const lowp float histBinWidth = 1.0 / 256.0;
void main()
{

    float raw_luminance = texture2D(u_Texture0,texCoords).r;
    vec2 hist_coord = vec2(raw_luminance, 0.0 );
    vec2 point0 = vec2(hist_coord.s - 2.0*histBinWidth,hist_coord.t);
    vec2 point1 = vec2(hist_coord.s - histBinWidth,hist_coord.t);
    vec2 point2 = vec2(hist_coord.s + histBinWidth,hist_coord.t);
    vec2 point3 = vec2(hist_coord.s + 2.0*histBinWidth,hist_coord.t);
    float histeq_luminance = 0.2*(texture2D(u_Texture2,hist_coord).r +
    texture2D(u_Texture2,point0).r+
    texture2D(u_Texture2,point1).r+
    texture2D(u_Texture2,point2).r+
    texture2D(u_Texture2,point3).r)
    / texture2D(u_Texture2,max_coord).r;
    vec3 rgb_color = coeffs*(vec3( histeq_luminance ,texture2D(u_Texture1,texCoords).ra) - offset);
    gl_FragColor = vec4(rgb_color,1.0);
}