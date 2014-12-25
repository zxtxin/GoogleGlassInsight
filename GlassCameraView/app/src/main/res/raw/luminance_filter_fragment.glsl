precision mediump float;
uniform sampler2D u_Texture0;
varying vec2 texCoords;
 void main()
 {
     float tmp = 1.164*(texture2D(u_Texture0,texCoords).r-0.0625);
     gl_FragColor = vec4(tmp,tmp,tmp,1.0);
 }