<template>
	<div id="destination-photo">
			<v-speed-dial
				v-model="fab"
				bottom
				right
				direction="top"
				:open-on-hover="false"
				transition="slide-y-reverse-transition"
				id="permissions-button">
				<template v-slot:activator>
					<v-btn v-model="fab" color="blue darken-2" dark fab>
						<v-icon>{{photo.isPublic ? 'public' : 'lock'}}</v-icon>
						<v-icon>close</v-icon>
					</v-btn>
				</template>
				
				<v-btn v-if="!photo.isPublic" fab dark small color="green">
					<v-icon>public</v-icon>
				</v-btn>

				<v-btn v-if="photo.isPublic" fab dark small color="indigo">
					<v-icon>lock</v-icon>
				</v-btn>
    </v-speed-dial>

		<v-img
			src="http://barkpost-assets.s3.amazonaws.com/wp-content/uploads/2013/11/plainDoge.jpg"
			aspect-ratio="1"
		/>
	</div>
</template>

<script>
export default {
    props: {
        photo: {
            photoId: {
							type: Number,
							required: true
						},
            isPublic: {
							type: Boolean,
							required: true
						}
        }
		},
		data() {
			return {
				fab: false
			}
		}
}
</script>

<style lang="scss" scoped>
#destination-photo {
	position: relative;

	#permissions-button {
		z-index: 1;
		position: absolute;
	}
}
</style>