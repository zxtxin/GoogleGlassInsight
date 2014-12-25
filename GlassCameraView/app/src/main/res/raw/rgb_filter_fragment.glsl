precision mediump float;
 uniform sampler2D u_Texture0;
 uniform sampler2D u_Texture1;
 varying vec2 texCoords;
 const mat3 coeffs = mat3(
 1.164,  1.164,  1.164,
 1.596, -0.813,  0.0,
 0.0  , -0.391,  2.018 );
 const vec3 offset = vec3(0.0625, 0.5, 0.5);
 void main()
 {
     vec3 rgb_color = coeffs*(vec3(texture2D(u_Texture0,texCoords).r,texture2D(u_Texture1,texCoords).ra) - offset);
     gl_FragColor = vec4(rgb_color,1.0);
 }